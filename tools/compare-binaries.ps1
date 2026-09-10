<#
.SYNOPSIS
    Müşteri bazlı jar çıktılarının gerçekten farklı olup olmadığını karşılaştırır.

.DESCRIPTION
    Her müşteri için ayrı bir build alır, üretilen jar'ları saklar ve
    içeriklerini karşılaştırır:

      - hangi ekran paketleri hangi jar'da var
      - jar boyutu ve sınıf sayısı
      - ikili karşılaştırma: sadece A'da olan / sadece B'de olan sınıflar
      - ortak sınıfların içerikleri (MD5) aynı mı

    Sonuç hem konsola yazılır hem de HTML rapor olarak kaydedilir.

.EXAMPLE
    .\tools\compare-binaries.ps1

.EXAMPLE
    .\tools\compare-binaries.ps1 -Customers customerA,customerB -Open

.EXAMPLE
    .\tools\compare-binaries.ps1 -SkipBuild
#>

[CmdletBinding()]
param(
    # Karşılaştırılacak müşteriler. Boş bırakılırsa src/customers altındaki hepsi.
    [string[]] $Customers,

    # Build almadan, daha önce üretilmiş jar'ları kullan.
    [switch] $SkipBuild,

    # Rapor hazır olunca tarayıcıda aç.
    [switch] $Open
)

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.IO.Compression.FileSystem

# =====================================================================
# YOLLAR
# =====================================================================

$projectRoot  = Split-Path -Parent $PSScriptRoot
$customersDir = Join-Path $projectRoot 'src\customers'
$screensDir   = Join-Path $projectRoot 'src\screens'
# NOT: 'build' altinda olamaz; her musteri icin 'gradlew clean' calisiyor
# ve build dizinini komple siliyor.
$outDir       = Join-Path $projectRoot 'binary-comparison'
$reportPath   = Join-Path $outDir 'index.html'

if (-not $Customers -or $Customers.Count -eq 0) {
    $Customers = Get-ChildItem $customersDir -Directory |
        Select-Object -ExpandProperty Name |
        Sort-Object
}

if ($Customers.Count -lt 2) {
    throw "Karşılaştırma için en az iki müşteri gerekli. Bulunan: $($Customers -join ', ')"
}

if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

$allScreens = Get-ChildItem $screensDir -Directory |
    Select-Object -ExpandProperty Name |
    Sort-Object

# =====================================================================
# BUILD
# =====================================================================

if (-not $SkipBuild) {

    foreach ($customer in $Customers) {

        Write-Host "[build] $customer" -ForegroundColor Cyan

        # Gradle, JVM uyarilarini stderr'e yazar. PowerShell 5.1 bunlari
        # ErrorRecord'a cevirdigi icin ErrorActionPreference gecici olarak
        # gevsetilir; basari/basarisizlik cikis kodundan okunur.

        $ErrorActionPreference = 'Continue'

        & (Join-Path $projectRoot 'gradlew.bat') -p $projectRoot clean jar "-Pcustomer=$customer" --console=plain --quiet 2>&1 |
            Where-Object { "$_" -notmatch '^WARNING|restricted method|native-access|RemoteException' } |
            ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }

        $exitCode = $LASTEXITCODE

        $ErrorActionPreference = 'Stop'

        if ($exitCode -ne 0) {
            throw "Build başarısız: $customer"
        }

        $built = Get-ChildItem (Join-Path $projectRoot 'build\libs') -Filter *.jar |
            Select-Object -First 1

        if (-not $built) {
            throw "Jar üretilmedi: $customer"
        }

        Copy-Item $built.FullName (Join-Path $outDir "$customer.jar") -Force
    }
}

# =====================================================================
# JAR OKUMA
# =====================================================================

function Get-JarEntries {

    param([string] $JarPath)

    $entries = @{}

    $zip = [System.IO.Compression.ZipFile]::OpenRead($JarPath)

    try {

        $md5 = [System.Security.Cryptography.MD5]::Create()

        foreach ($entry in $zip.Entries) {

            if ($entry.FullName.EndsWith('/')) { continue }

            $stream = $entry.Open()

            try {
                $hash = [System.BitConverter]::ToString($md5.ComputeHash($stream)).Replace('-', '')
            }
            finally {
                $stream.Dispose()
            }

            $entries[$entry.FullName] = [pscustomobject]@{
                Size = $entry.Length
                Hash = $hash
            }
        }
    }
    finally {
        $zip.Dispose()
    }

    return $entries
}

$data = [ordered]@{}

foreach ($customer in $Customers) {

    $jarPath = Join-Path $outDir "$customer.jar"

    if (-not (Test-Path $jarPath)) {
        throw "Jar bulunamadı: $jarPath  (-SkipBuild kullandıysanız önce build alın)"
    }

    $entries = Get-JarEntries -JarPath $jarPath

    $screensPresent = @()

    foreach ($screen in $allScreens) {

        # ToLower() degil ToLowerInvariant(): Turkce locale'de
        # "PORTFOLIO".ToLower() -> "portfolio" degil "portfolıo" verir.
        $prefix = "dashboard/$($screen.ToLowerInvariant())/"

        $hit = $entries.Keys | Where-Object { $_.StartsWith($prefix) } | Select-Object -First 1

        if ($hit) { $screensPresent += $screen }
    }

    $data[$customer] = [pscustomobject]@{
        JarPath    = $jarPath
        JarSize    = (Get-Item $jarPath).Length
        Entries    = $entries
        ClassCount = ($entries.Keys | Where-Object { $_.EndsWith('.class') }).Count
        Screens    = $screensPresent
    }
}

# =====================================================================
# IKILI KARSILASTIRMA
# =====================================================================

$pairs = @()

for ($i = 0; $i -lt $Customers.Count; $i++) {
    for ($j = $i + 1; $j -lt $Customers.Count; $j++) {

        $a = $Customers[$i]
        $b = $Customers[$j]

        $keysA = $data[$a].Entries.Keys
        $keysB = $data[$b].Entries.Keys

        $onlyA  = @($keysA | Where-Object { -not $data[$b].Entries.ContainsKey($_) } | Sort-Object)
        $onlyB  = @($keysB | Where-Object { -not $data[$a].Entries.ContainsKey($_) } | Sort-Object)
        $common = @($keysA | Where-Object { $data[$b].Entries.ContainsKey($_) })

        $changed = @($common | Where-Object {
            $data[$a].Entries[$_].Hash -ne $data[$b].Entries[$_].Hash
        } | Sort-Object)

        $pairs += [pscustomobject]@{
            A         = $a
            B         = $b
            OnlyA     = $onlyA
            OnlyB     = $onlyB
            Changed   = $changed
            Identical = ($onlyA.Count -eq 0 -and $onlyB.Count -eq 0 -and $changed.Count -eq 0)
        }
    }
}

# =====================================================================
# KONSOL
# =====================================================================

Write-Host ""
Write-Host "==============================================================" -ForegroundColor DarkGray
Write-Host " BINARY KARSILASTIRMA" -ForegroundColor White
Write-Host "==============================================================" -ForegroundColor DarkGray
Write-Host ""

$summary = foreach ($customer in $Customers) {
    [pscustomobject]@{
        'Musteri'  = $customer
        'Jar (KB)' = [math]::Round($data[$customer].JarSize / 1KB, 1)
        'Sinif'    = $data[$customer].ClassCount
        'Ekranlar' = ($data[$customer].Screens -join ', ')
    }
}

$summary | Format-Table -AutoSize | Out-String | Write-Host

Write-Host " EKRAN MATRISI" -ForegroundColor White
Write-Host ""

$header = "  {0,-14}" -f ''
foreach ($customer in $Customers) { $header += ("{0,-16}" -f $customer) }
Write-Host $header -ForegroundColor DarkGray

foreach ($screen in $allScreens) {

    $line = "  {0,-14}" -f $screen

    foreach ($customer in $Customers) {
        if ($data[$customer].Screens -contains $screen) {
            $line += ("{0,-16}" -f "VAR")
        }
        else {
            $line += ("{0,-16}" -f "-")
        }
    }

    Write-Host $line
}

Write-Host ""
Write-Host " IKILI KARSILASTIRMA" -ForegroundColor White
Write-Host ""

foreach ($pair in $pairs) {

    if ($pair.Identical) {
        Write-Host ("  {0} <-> {1} : AYNI" -f $pair.A, $pair.B) -ForegroundColor Yellow
    }
    else {
        Write-Host ("  {0} <-> {1} : FARKLI  (yalniz {0}: {2}, yalniz {1}: {3}, icerigi degisen: {4})" -f `
            $pair.A, $pair.B, $pair.OnlyA.Count, $pair.OnlyB.Count, $pair.Changed.Count) -ForegroundColor Green
    }
}

Write-Host ""

# =====================================================================
# HTML RAPOR
# =====================================================================

function ConvertTo-HtmlText {
    param([string] $Text)
    if ($null -eq $Text) { return '' }
    return $Text.Replace('&', '&amp;').Replace('<', '&lt;').Replace('>', '&gt;')
}

$sb = New-Object System.Text.StringBuilder

[void]$sb.AppendLine('<!doctype html><html lang="tr"><head><meta charset="utf-8">')
[void]$sb.AppendLine('<meta name="viewport" content="width=device-width,initial-scale=1">')
[void]$sb.AppendLine('<title>Binary Karsilastirma</title><style>')
[void]$sb.AppendLine(@'
:root { --bg:#ffffff; --fg:#14171a; --muted:#606a76; --line:#e3e7ec;
        --yes:#0b7a3b; --yes-bg:#e6f5ec; --no:#8a9099; --no-bg:#f4f6f8;
        --warn:#8a5a00; --accent:#0b4f9e; }
* { box-sizing:border-box; }
body { margin:0; padding:32px; background:var(--bg); color:var(--fg);
       font:14px/1.55 -apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif; }
h1 { font-size:22px; margin:0 0 4px; letter-spacing:-.01em; }
h2 { font-size:15px; margin:34px 0 12px; text-transform:uppercase;
     letter-spacing:.08em; color:var(--muted); font-weight:600; }
.sub { color:var(--muted); margin:0 0 8px; }
.wrap { overflow-x:auto; }
table { border-collapse:collapse; width:100%; margin:0 0 8px; font-size:13px; }
th,td { padding:8px 12px; border-bottom:1px solid var(--line); text-align:left;
        vertical-align:top; white-space:nowrap; }
th { font-weight:600; color:var(--muted); font-size:12px;
     text-transform:uppercase; letter-spacing:.05em; }
td.num { text-align:right; font-variant-numeric:tabular-nums; }
.tag { display:inline-block; padding:2px 9px; border-radius:99px;
       font-size:12px; font-weight:600; }
.yes { background:var(--yes-bg); color:var(--yes); }
.no  { background:var(--no-bg);  color:var(--no); }
.diff { color:var(--yes); font-weight:600; }
.same { color:var(--warn); font-weight:600; }
code { font:12px/1.5 ui-monospace,SFMono-Regular,Consolas,monospace; }
details { border:1px solid var(--line); border-radius:8px; padding:10px 14px;
          margin:0 0 10px; }
summary { cursor:pointer; font-weight:600; }
ul.files { margin:10px 0 0; padding-left:20px; }
ul.files li { font:12px/1.7 ui-monospace,SFMono-Regular,Consolas,monospace;
              color:var(--muted); white-space:nowrap; }
footer { margin-top:40px; color:var(--muted); font-size:12px;
         border-top:1px solid var(--line); padding-top:14px; }
'@)
[void]$sb.AppendLine('</style></head><body>')

[void]$sb.AppendLine('<h1>Binary Karşılaştırma</h1>')
[void]$sb.AppendLine("<p class=""sub"">Üretim: $(Get-Date -Format 'dd.MM.yyyy HH:mm') &nbsp;·&nbsp; $($Customers.Count) müşteri &nbsp;·&nbsp; kaynak: <code>build/binary-comparison/</code></p>")

# --- ozet ---
[void]$sb.AppendLine('<h2>Özet</h2><div class="wrap"><table><thead><tr>')
[void]$sb.AppendLine('<th>Müşteri</th><th>Jar boyutu</th><th>Sınıf sayısı</th><th>Derlenen ekranlar</th>')
[void]$sb.AppendLine('</tr></thead><tbody>')

foreach ($customer in $Customers) {
    $d = $data[$customer]
    $kb = [math]::Round($d.JarSize / 1KB, 1)
    [void]$sb.AppendLine("<tr><td><strong>$(ConvertTo-HtmlText $customer)</strong></td><td class=""num"">$kb KB</td><td class=""num"">$($d.ClassCount)</td><td>$(ConvertTo-HtmlText ($d.Screens -join ', '))</td></tr>")
}

[void]$sb.AppendLine('</tbody></table></div>')

# --- ekran matrisi ---
[void]$sb.AppendLine('<h2>Ekran matrisi</h2><div class="wrap"><table><thead><tr><th>Ekran</th>')

foreach ($customer in $Customers) {
    [void]$sb.AppendLine("<th>$(ConvertTo-HtmlText $customer)</th>")
}

[void]$sb.AppendLine('</tr></thead><tbody>')

foreach ($screen in $allScreens) {

    [void]$sb.AppendLine("<tr><td><code>$(ConvertTo-HtmlText $screen)</code></td>")

    foreach ($customer in $Customers) {
        if ($data[$customer].Screens -contains $screen) {
            [void]$sb.AppendLine('<td><span class="tag yes">binary&#39;de var</span></td>')
        }
        else {
            [void]$sb.AppendLine('<td><span class="tag no">yok</span></td>')
        }
    }

    [void]$sb.AppendLine('</tr>')
}

[void]$sb.AppendLine('</tbody></table></div>')

# --- ikili karsilastirma ---
[void]$sb.AppendLine('<h2>İkili karşılaştırma</h2><div class="wrap"><table><thead><tr>')
[void]$sb.AppendLine('<th>A</th><th>B</th><th>Sonuç</th><th>Yalnız A&#39;da</th><th>Yalnız B&#39;de</th><th>İçeriği farklı</th>')
[void]$sb.AppendLine('</tr></thead><tbody>')

foreach ($pair in $pairs) {

    if ($pair.Identical) {
        $verdict = '<span class="same">AYNI</span>'
    }
    else {
        $verdict = '<span class="diff">FARKLI</span>'
    }

    [void]$sb.AppendLine("<tr><td><strong>$(ConvertTo-HtmlText $pair.A)</strong></td><td><strong>$(ConvertTo-HtmlText $pair.B)</strong></td><td>$verdict</td><td class=""num"">$($pair.OnlyA.Count)</td><td class=""num"">$($pair.OnlyB.Count)</td><td class=""num"">$($pair.Changed.Count)</td></tr>")
}

[void]$sb.AppendLine('</tbody></table></div>')

# --- detay ---
[void]$sb.AppendLine('<h2>Detay</h2>')

foreach ($pair in $pairs) {

    if ($pair.Identical) { continue }

    [void]$sb.AppendLine("<details><summary>$(ConvertTo-HtmlText $pair.A) &harr; $(ConvertTo-HtmlText $pair.B)</summary>")

    if ($pair.OnlyA.Count -gt 0) {
        [void]$sb.AppendLine("<p class=""sub"">Yalnızca <strong>$(ConvertTo-HtmlText $pair.A)</strong> içinde:</p><ul class=""files"">")
        foreach ($f in $pair.OnlyA) { [void]$sb.AppendLine("<li>$(ConvertTo-HtmlText $f)</li>") }
        [void]$sb.AppendLine('</ul>')
    }

    if ($pair.OnlyB.Count -gt 0) {
        [void]$sb.AppendLine("<p class=""sub"">Yalnızca <strong>$(ConvertTo-HtmlText $pair.B)</strong> içinde:</p><ul class=""files"">")
        foreach ($f in $pair.OnlyB) { [void]$sb.AppendLine("<li>$(ConvertTo-HtmlText $f)</li>") }
        [void]$sb.AppendLine('</ul>')
    }

    if ($pair.Changed.Count -gt 0) {
        [void]$sb.AppendLine('<p class="sub">Adı aynı, içeriği farklı:</p><ul class="files">')
        foreach ($f in $pair.Changed) { [void]$sb.AppendLine("<li>$(ConvertTo-HtmlText $f)</li>") }
        [void]$sb.AppendLine('</ul>')
    }

    [void]$sb.AppendLine('</details>')
}

[void]$sb.AppendLine('<footer>Karşılaştırma jar içeriği üzerinden yapılır: dosya listesi ve her dosyanın MD5 özeti. ')
[void]$sb.AppendLine('&quot;Yok&quot; işaretli bir ekranın kodu o build&#39;e hiç derlenmemiştir.</footer>')
[void]$sb.AppendLine('</body></html>')

$sb.ToString() | Out-File -FilePath $reportPath -Encoding utf8

Write-Host " Rapor: $reportPath" -ForegroundColor Cyan
Write-Host ""

if ($Open) {
    Start-Process $reportPath
}
