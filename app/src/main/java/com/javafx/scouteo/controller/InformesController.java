package com.javafx.scouteo.controller;

import com.javafx.scouteo.util.ConexionBD;
import com.javafx.scouteo.utils.StageUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para la gestión y visualización de informes JasperReports
 */
public class InformesController {

    @FXML
    private StackPane webViewContainer;

    private WebView webViewInforme;

    @FXML
    private VBox vboxMensajeInicial;

    @FXML
    private CheckBox chkNuevaVentana;

    @FXML
    private Label lblEstado;

    @FXML
    private Button btnInformeJugadores;

    @FXML
    private Button btnInformePartidos;

    @FXML
    private Button btnInformeEstadisticas;

    @FXML
    private Button btnInformeGraficos;

    @FXML
    private Button btnExportarPDF;

    @FXML
    private ComboBox<String> cmbFiltroTipo;

    @FXML
    private ComboBox<String> cmbFiltroValor;

    private String ultimoArchivoHTML = null;
    private String ultimoArchivoPDF = null;
    private String ultimoTituloInforme = "informe";

    @FXML
    public void initialize() {
        // Inicializar ComboBox de tipo de filtro
        inicializarFiltros();
        // Mostrar mensaje inicial
        vboxMensajeInicial.setVisible(true);
        vboxMensajeInicial.setManaged(true);

        // Crear WebView dinámicamente con los exports correctos
        try {
            webViewInforme = new WebView();
            webViewInforme.setPrefWidth(850);
            webViewInforme.setPrefHeight(500);
            webViewInforme.setVisible(false);
            webViewInforme.setManaged(false);
            webViewContainer.getChildren().add(0, webViewInforme);
        } catch (Exception e) {
            System.err.println("Error al crear WebView: " + e.getMessage());
            e.printStackTrace();
            webViewInforme = null;
        }

        actualizarEstado("Listo para generar informes", "#4CAF50");
    }

    /**
     * Inicializa los ComboBox de filtros
     */
    private void inicializarFiltros() {
        // ComboBox de tipo de filtro
        ObservableList<String> tiposFiltro = FXCollections.observableArrayList(
            "Categoría", "Posición"
        );
        cmbFiltroTipo.setItems(tiposFiltro);

        // Listener para cambiar los valores según el tipo seleccionado
        cmbFiltroTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarValoresFiltro(newVal);
            }
        });
    }

    /**
     * Actualiza los valores del ComboBox según el tipo de filtro seleccionado
     */
    private void actualizarValoresFiltro(String tipoFiltro) {
        ObservableList<String> valores = FXCollections.observableArrayList();

        if ("Categoría".equals(tipoFiltro)) {
            valores.addAll("Benjamín", "Alevín", "Infantil", "Cadete");
        } else if ("Posición".equals(tipoFiltro)) {
            valores.addAll("POR", "DEF", "MED", "DEL");
        }

        cmbFiltroValor.setItems(valores);
        cmbFiltroValor.getSelectionModel().clearSelection();
        cmbFiltroValor.setPromptText("Seleccionar " + tipoFiltro.toLowerCase());
    }

    /**
     * Genera informe filtrado desde la interfaz de usuario
     */
    @FXML
    private void generarInformeFiltradoUI() {
        String tipoFiltro = cmbFiltroTipo.getValue();
        String valorFiltro = cmbFiltroValor.getValue();

        // Validar que se hayan seleccionado ambos valores
        if (tipoFiltro == null || tipoFiltro.isEmpty()) {
            mostrarAlerta("Por favor, selecciona el tipo de filtro");
            return;
        }

        if (valorFiltro == null || valorFiltro.isEmpty()) {
            mostrarAlerta("Por favor, selecciona un valor para filtrar");
            return;
        }

        // Construir título del informe
        String titulo;
        if ("Categoría".equals(tipoFiltro)) {
            titulo = "Jugadores Categoría " + valorFiltro;
        } else {
            String posicionNombre = obtenerNombrePosicion(valorFiltro);
            titulo = "Jugadores - " + posicionNombre;
        }

        // Generar el informe
        generarInformeFiltrado(valorFiltro, titulo);
    }

    /**
     * Obtiene el nombre completo de la posición
     */
    private String obtenerNombrePosicion(String codigo) {
        switch (codigo) {
            case "POR": return "Porteros";
            case "DEF": return "Defensas";
            case "MED": return "Medios";
            case "DEL": return "Delanteros";
            default: return codigo;
        }
    }

    /**
     * Genera informe de listado de jugadores
     */
    @FXML
    private void generarInformeJugadores() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", "Listado de Jugadores");

        lanzaInforme("/reports/Simple_Blue.jasper", parametros);
    }

    /**
     * Genera informe de listado de partidos
     */
    @FXML
    private void generarInformePartidos() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", "Listado de Partidos");

        lanzaInforme("/reports/partidos.jasper", parametros);
    }

    /**
     * Genera informe con estadísticas completas (SQL compuesta)
     */
    @FXML
    private void generarInformeEstadisticas() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", "Estadísticas Completas de Jugadores");

        lanzaInforme("/reports/estadisticas_completas.jasper", parametros);
    }

    /**
     * Genera informe con gráficas
     */
    @FXML
    private void generarInformeGraficos() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", "Distribución de Jugadores por Posición");

        lanzaInforme("/reports/graficos_posiciones.jasper", parametros);
    }

    /**
     * Genera informe de jugadores con logo (todos los jugadores)
     */
    @FXML
    private void generarInformeJugadoresConLogo() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", "Listado Completo de Jugadores");
        parametros.put("CONDICION", null); // null = sin filtro

        lanzaInforme("/reports/jugadores_filtrado_simple.jasper", parametros);
    }

    /**
     * Genera informe filtrado de jugadores según condición
     * @param condicion Valor para filtrar (categoría o posición). Ej: "Cadete", "Infantil", "DEF", "POR"
     * @param titulo Título personalizado para el informe
     */
    @FXML
    private void generarInformeFiltrado(String condicion, String titulo) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", titulo);
        parametros.put("CONDICION", condicion);

        lanzaInforme("/reports/jugadores_filtrado_simple.jasper", parametros);
    }

    /**
     * Ejemplo: Genera informe de jugadores de la categoría Cadete
     */
    @FXML
    private void generarInformeCadete() {
        generarInformeFiltrado("Cadete", "Jugadores Categoría Cadete");
    }

    /**
     * Ejemplo: Genera informe de jugadores de la categoría Infantil
     */
    @FXML
    private void generarInformeInfantil() {
        generarInformeFiltrado("Infantil", "Jugadores Categoría Infantil");
    }

    /**
     * Ejemplo: Genera informe de jugadores defensas
     */
    @FXML
    private void generarInformeDefensas() {
        generarInformeFiltrado("DEF", "Jugadores Defensas");
    }

    /**
     * Ejemplo: Genera informe de jugadores porteros
     */
    @FXML
    private void generarInformePorteros() {
        generarInformeFiltrado("POR", "Jugadores Porteros");
    }

    /**
     * Exporta el último informe generado a PDF
     */
    @FXML
    private void exportarPDF() {
        if (ultimoArchivoPDF == null) {
            mostrarAlerta("No hay ningún informe generado para exportar");
            return;
        }

        actualizarEstado("PDF exportado correctamente", "#4CAF50");
        mostrarAlerta("El informe ya está guardado como '" + ultimoTituloInforme + ".pdf' en la carpeta del proyecto");
    }

    /**
     * Limpia la vista del WebView
     */
    @FXML
    private void limpiarVista() {
        if (webViewInforme != null) {
            webViewInforme.getEngine().loadContent("");
            webViewInforme.setVisible(false);
            webViewInforme.setManaged(false);
        }
        vboxMensajeInicial.setVisible(true);
        vboxMensajeInicial.setManaged(true);
        ultimoArchivoHTML = null;
        actualizarEstado("Vista limpiada", "#4CAF50");
    }

    /**
     * Función genérica para lanzar informes JasperReports
     * Basada en la implementación del proyecto DI-T3-main
     * @param rutaInforme Ruta del archivo .jasper (ej: "/reports/informe.jasper")
     * @param parametros HashMap con parámetros del informe
     */
    private void lanzaInforme(String rutaInforme, Map<String, Object> parametros) {
        try {
            actualizarEstado("Generando informe...", "#FF9800");

            // 1. CARGA el archivo .jasper (compilado de .jrxml)
            JasperReport report = (JasperReport) JRLoader.loadObject(
                getClass().getResourceAsStream(rutaInforme)
            );

            // 2. Obtener conexión a la base de datos
            Connection conBD = ConexionBD.getConexion();

            // Verificar conexión
            if (conBD == null || conBD.isClosed()) {
                mostrarAlerta("Error: No hay conexión con la base de datos");
                actualizarEstado("Error de conexión", "#F44336");
                return;
            }

            // 3. RELLENA el informe con datos de la BD
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parametros, conBD);

            if (!jasperPrint.getPages().isEmpty()) {

                // Obtener título del informe de los parámetros si existe
                String tituloInforme = (String) parametros.getOrDefault("TituloInforme", "informe");

                // Limpiar el título para usarlo como nombre de archivo
                String nombreArchivo = limpiarNombreArchivo(tituloInforme);

                // Guardar título para el botón de exportar
                ultimoTituloInforme = nombreArchivo;

                // 4. EXPORTA a PDF con nombre único
                String pdfOutputPath = "./" + nombreArchivo + ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, pdfOutputPath);
                ultimoArchivoPDF = pdfOutputPath;

                // 5. EXPORTA a HTML
                String outputHtmlFile = "./" + nombreArchivo + ".html";
                JasperExportManager.exportReportToHtmlFile(jasperPrint, outputHtmlFile);
                ultimoArchivoHTML = outputHtmlFile;

                // 6. MUESTRA en WebView
                if (chkNuevaVentana.isSelected() || webViewInforme == null) {
                    // Opción 1: Abrir en nueva ventana modal
                    WebView wvnuevo = new WebView();
                    wvnuevo.getEngine().load(new File(outputHtmlFile).toURI().toString());
                    StackPane stackPane = new StackPane(wvnuevo);
                    Scene scene = new Scene(stackPane, 900, 700);
                    Stage stage = new Stage();
                    stage.setTitle("Vista de Informe - Scouteo");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(true);
                    stage.setScene(scene);
                    stage.show();
                    actualizarEstado("Informe abierto en nueva ventana", "#4CAF50");
                } else {
                    // Opción 2: Mostrar en WebView incrustado (en la ventana principal)
                    webViewInforme.getEngine().load(new File(outputHtmlFile).toURI().toString());
                    webViewInforme.setVisible(true);
                    webViewInforme.setManaged(true);
                    vboxMensajeInicial.setVisible(false);
                    vboxMensajeInicial.setManaged(false);
                    actualizarEstado("Informe generado y guardado como " + nombreArchivo + ".pdf", "#4CAF50");
                }
            } else {
                mostrarAlerta("La búsqueda no generó páginas");
                actualizarEstado("Sin resultados", "#FF9800");
            }

        } catch (JRException e) {
            e.printStackTrace();
            mostrarAlerta("Error al generar informe: " + e.getMessage());
            actualizarEstado("Error al generar informe", "#F44336");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error inesperado: " + e.getMessage());
            actualizarEstado("Error inesperado", "#F44336");
        }
    }

    /**
     * Actualiza el label de estado con un mensaje y color
     */
    private void actualizarEstado(String mensaje, String color) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    /**
     * Muestra un diálogo de alerta
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informes");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Agregar icono a la ventana de alerta
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        StageUtils.setAppIcon(alertStage);

        alert.showAndWait();
    }

    /**
     * Limpia el título del informe para usarlo como nombre de archivo
     * Elimina caracteres no válidos y convierte espacios en guiones bajos
     */
    private String limpiarNombreArchivo(String titulo) {
        // Convertir a minúsculas
        String limpio = titulo.toLowerCase();

        // Reemplazar espacios por guiones bajos
        limpio = limpio.replace(" ", "_");

        // Eliminar caracteres especiales (mantener solo letras, números, guiones y guiones bajos)
        limpio = limpio.replaceAll("[^a-z0-9_-]", "");

        // Si quedó vacío, usar nombre por defecto
        if (limpio.isEmpty()) {
            limpio = "informe";
        }

        return limpio;
    }
}
