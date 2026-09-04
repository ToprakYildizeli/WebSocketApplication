package customerCreator

import customer.CustomerGenerator
import customer.DashboardScreen
import customer.ScreenCatalog
import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.koin.java.KoinJavaComponent.getKoin
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CustomerCreatorApp : Application() {

    private val customersPath: Path =
        Paths.get("src/customers")

    private val screenCatalog =
        getKoin().get<ScreenCatalog>()

    override fun start(stage: Stage) {

        // =====================================================
        // TITLE
        // =====================================================

        val title =
            Label("Customer Management")

        title.style = """
            -fx-text-fill: #F8FAFC;
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """.trimIndent()

        val subtitle =
            Label("Manage customer configurations and features")

        subtitle.style = """
            -fx-text-fill: #94A3B8;
            -fx-font-size: 12px;
        """.trimIndent()

        // =====================================================
        // TOGGLE
        // =====================================================

        val addToggle =
            ToggleButton("ADD CUSTOMER")

        val deleteToggle =
            ToggleButton("DELETE CUSTOMER")

        val toggleGroup =
            ToggleGroup()

        addToggle.toggleGroup =
            toggleGroup

        deleteToggle.toggleGroup =
            toggleGroup

        addToggle.isSelected =
            true

        styleToggle(
            addToggle,
            true
        )

        styleToggle(
            deleteToggle,
            false
        )

        val toggleBox =
            HBox(0.0)

        toggleBox.alignment =
            Pos.CENTER

        toggleBox.maxWidth =
            Double.MAX_VALUE

        toggleBox.children.addAll(
            addToggle,
            deleteToggle
        )

        // =====================================================
        // ADD SECTION
        // =====================================================

        val addTitle =
            Label("Add New Customer")

        addTitle.style = """
            -fx-text-fill: #F8FAFC;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """.trimIndent()

        val addSubtitle =
            Label("Enter customer information and select available screens.")

        addSubtitle.style = """
            -fx-text-fill: #94A3B8;
            -fx-font-size: 12px;
        """.trimIndent()

        val customerNameLabel =
            Label("Customer Name")

        customerNameLabel.style = """
            -fx-text-fill: #CBD5E1;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
        """.trimIndent()

        val customerNameField =
            TextField()

        customerNameField.promptText =
            "Customer name"

        customerNameField.style = """
            -fx-background-color: #1E293B;
            -fx-background-radius: 8px;
            -fx-border-color: #334155;
            -fx-border-radius: 8px;
            -fx-text-fill: #F8FAFC;
            -fx-prompt-text-fill: #64748B;
            -fx-font-size: 14px;
            -fx-padding: 10px;
        """.trimIndent()

        val screensLabel =
            Label("Dashboard Screens")

        screensLabel.style = """
            -fx-text-fill: #CBD5E1;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
        """.trimIndent()

        val screenContainer =
            VBox(4.0)

        screenContainer.style = """
            -fx-background-color: #111827;
            -fx-background-radius: 10px;
            -fx-padding: 12px;
        """.trimIndent()

        val checkBoxes =
            mutableMapOf<DashboardScreen, CheckBox>()

        screenCatalog.available().forEach { screen ->

            val checkBox =
                CheckBox(screen.name)

            checkBox.style = """
                -fx-text-fill: #E2E8F0;
                -fx-font-size: 13px;
                -fx-padding: 7px;
            """.trimIndent()

            checkBoxes[screen] =
                checkBox

            screenContainer.children.add(
                checkBox
            )
        }

        val addButton =
            Button("Add New Customer")

        addButton.maxWidth =
            Double.MAX_VALUE

        addButton.prefHeight =
            42.0

        addButton.style = """
            -fx-background-color: #2563EB;
            -fx-background-radius: 8px;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """.trimIndent()

        val addSection =
            VBox(12.0)

        addSection.children.addAll(
            addTitle,
            addSubtitle,
            customerNameLabel,
            customerNameField,
            screensLabel,
            screenContainer,
            addButton
        )

        // =====================================================
        // DELETE SECTION
        // =====================================================

        val deleteTitle =
            Label("Delete Customer")

        deleteTitle.style = """
            -fx-text-fill: #F8FAFC;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """.trimIndent()

        val deleteSubtitle =
            Label("Select a customer to permanently remove.")

        deleteSubtitle.style = """
            -fx-text-fill: #94A3B8;
            -fx-font-size: 12px;
        """.trimIndent()

        val customerComboBox =
            ComboBox<String>()

        customerComboBox.maxWidth =
            Double.MAX_VALUE

        customerComboBox.promptText =
            "Select customer"

        customerComboBox.style = """
            -fx-background-color: #1E293B;
            -fx-background-radius: 8px;
            -fx-border-color: #334155;
            -fx-border-radius: 8px;
            -fx-text-fill: #F8FAFC;
        """.trimIndent()

        loadCustomers(
            customerComboBox
        )

        val deleteButton =
            Button("Delete Customer")

        deleteButton.maxWidth =
            Double.MAX_VALUE

        deleteButton.prefHeight =
            42.0

        deleteButton.style = """
            -fx-background-color: #DC2626;
            -fx-background-radius: 8px;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """.trimIndent()

        val deleteSection =
            VBox(12.0)

        deleteSection.children.addAll(
            deleteTitle,
            deleteSubtitle,
            customerComboBox,
            deleteButton
        )

        deleteSection.isVisible =
            false

        deleteSection.isManaged =
            false

        // =====================================================
        // ADD ACTION
        // =====================================================

        addButton.setOnAction {

            val customerName =
                customerNameField.text.trim()

            val selectedScreens =
                checkBoxes
                    .filter { entry ->
                        entry.value.isSelected
                    }
                    .keys
                    .toSet()

            try {

                CustomerGenerator(
                    customersPath
                ).generate(
                    customerName = customerName,
                    dashboardScreens = selectedScreens
                )

                showSuccess(
                    "Customer '$customerName' created successfully."
                )

                stage.close()

            } catch (exception: Exception) {

                showError(
                    exception.message
                        ?: "Customer could not be created."
                )
            }
        }

        // =====================================================
        // DELETE ACTION
        // =====================================================

        deleteButton.setOnAction {

            val selectedCustomer =
                customerComboBox.value

            if (selectedCustomer == null) {

                showError(
                    "Please select a customer."
                )

                return@setOnAction
            }

            val confirmation =
                Alert(
                    Alert.AlertType.CONFIRMATION
                )

            confirmation.title =
                "Delete Customer"

            confirmation.headerText =
                "Delete $selectedCustomer?"

            confirmation.contentText =
                "This action cannot be undone."

            val result =
                confirmation.showAndWait()

            if (
                result.isPresent &&
                result.get() == ButtonType.OK
            ) {

                try {

                    val customerDirectory =
                        customersPath.resolve(
                            selectedCustomer
                        )

                    if (
                        !Files.exists(
                            customerDirectory
                        )
                    ) {

                        showError(
                            "Customer does not exist."
                        )

                        return@setOnAction
                    }

                    customerDirectory
                        .toFile()
                        .deleteRecursively()

                    showSuccess(
                        "Customer '$selectedCustomer' deleted successfully."
                    )

                    customerComboBox.items.remove(
                        selectedCustomer
                    )

                    customerComboBox.value =
                        null

                } catch (exception: Exception) {

                    showError(
                        exception.message
                            ?: "Customer could not be deleted."
                    )
                }
            }
        }

        // =====================================================
        // TOGGLE ACTION
        // =====================================================

        toggleGroup
            .selectedToggleProperty()
            .addListener { _, _, selectedToggle ->

                val addMode =
                    selectedToggle == addToggle

                addSection.isVisible =
                    addMode

                addSection.isManaged =
                    addMode

                deleteSection.isVisible =
                    !addMode

                deleteSection.isManaged =
                    !addMode

                styleToggle(
                    addToggle,
                    addMode
                )

                styleToggle(
                    deleteToggle,
                    !addMode
                )
            }

        // =====================================================
        // ROOT
        // =====================================================

        val root =
            VBox(18.0)

        root.padding =
            Insets(24.0)

        root.alignment =
            Pos.TOP_CENTER

        root.style = """
            -fx-background-color: #0F172A;
        """.trimIndent()

        root.children.addAll(
            title,
            subtitle,
            toggleBox,
            addSection,
            deleteSection
        )

        val scene =
            Scene(
                root,
                400.0,
                600.0
            )

        stage.title =
            "Customer Management"

        stage.scene =
            scene

        stage.isResizable =
            false

        stage.show()
    }

    // =========================================================
    // TOGGLE STYLE
    // =========================================================

    private fun styleToggle(
        button: ToggleButton,
        selected: Boolean
    ) {

        button.prefWidth =
            160.0

        button.prefHeight =
            40.0

        button.style =
            if (selected) {

                """
                -fx-background-color: #2563EB;
                -fx-text-fill: white;
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                -fx-background-radius: 8px;
                -fx-border-radius: 8px;
                -fx-border-color: #3B82F6;
                -fx-cursor: hand;
                """.trimIndent()

            } else {

                """
                -fx-background-color: #1E293B;
                -fx-text-fill: #94A3B8;
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                -fx-background-radius: 8px;
                -fx-border-radius: 8px;
                -fx-border-color: #334155;
                -fx-cursor: hand;
                """.trimIndent()
            }
    }

    // =========================================================
    // LOAD CUSTOMERS
    // =========================================================

    private fun loadCustomers(
        comboBox: ComboBox<String>
    ) {

        if (!Files.exists(customersPath)) {
            return
        }

        Files.list(customersPath).use { stream ->

            stream
                .filter {
                    Files.isDirectory(it)
                }
                .map {
                    it.fileName.toString()
                }
                .filter {
                    it.startsWith("customer")
                }
                .sorted()
                .forEach {
                    comboBox.items.add(it)
                }
        }
    }

    // =========================================================
    // ALERTS
    // =========================================================

    private fun showError(
        message: String
    ) {

        val alert =
            Alert(
                Alert.AlertType.ERROR
            )

        alert.title =
            "Error"

        alert.headerText =
            null

        alert.contentText =
            message

        alert.showAndWait()
    }

    private fun showSuccess(
        message: String
    ) {

        val alert =
            Alert(
                Alert.AlertType.INFORMATION
            )

        alert.title =
            "Success"

        alert.headerText =
            null

        alert.contentText =
            message

        alert.showAndWait()
    }
}
