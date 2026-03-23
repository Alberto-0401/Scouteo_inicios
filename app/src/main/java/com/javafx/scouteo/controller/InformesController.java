package com.javafx.scouteo.controller;

import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.util.ConexionBD;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
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
    private ComboBox<String> cmbFiltroTipo;

    @FXML
    private ComboBox<String> cmbFiltroValor;


    @FXML
    public void initialize() {
        inicializarFiltros();
        actualizarEstado("Listo para generar informes", "#4CAF50");
    }

    /**
     * Devuelve el equipo_id del entrenador en sesión, o null si es directiva.
     * Permite que los informes muestren solo los datos del equipo propio.
     */
    private Integer getEquipoIdParaInformes() {
        SesionUsuario sesion = SesionUsuario.getInstance();
        if (!sesion.esEntrenador()) return null;
        EquipoDAO dao = new EquipoDAO();
        java.util.List<Equipo> equipos = dao.obtenerPorEntrenador(sesion.getUsuarioActual().getId());
        return equipos.isEmpty() ? null : equipos.get(0).getId();
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
            valores.addAll("prebenjamin", "benjamin", "alevin", "infantil", "cadete", "juvenil", "primer_equipo", "senior", "veteranos");
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
        parametros.put("EQUIPO_ID", getEquipoIdParaInformes());

        lanzaInforme("/reports/Simple_Blue.jrxml", parametros);
    }

    /**
     * Genera informe de listado de partidos
     */
    @FXML
    private void generarInformePartidos() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", "Listado de Partidos");
        parametros.put("EQUIPO_ID", getEquipoIdParaInformes());

        lanzaInforme("/reports/partidos.jrxml", parametros);
    }

    /**
     * Genera informe con estadísticas completas (SQL compuesta)
     */
    @FXML
    private void generarInformeEstadisticas() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", "Estadísticas Completas de Jugadores");
        parametros.put("EQUIPO_ID", getEquipoIdParaInformes());

        lanzaInforme("/reports/estadisticas_completas.jrxml", parametros);
    }

    /**
     * Abre ventana con gráficas dinámicas (PieChart + BarChart JavaFX)
     */
    @FXML
    private void generarInformeGraficos() {
        try {
            actualizarEstado("Abriendo gráficos...", "#FF9800");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GraficosRendimiento.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 980, 700);
            Stage stage = new Stage();
            stage.setTitle("Gráficos de Rendimiento — Scouteo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.setScene(scene);
            StageUtils.setAppIcon(stage);
            actualizarEstado("Gráficos cargados", "#4CAF50");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir gráficos: " + e.getMessage());
            actualizarEstado("Error al abrir gráficos", "#F44336");
        }
    }

    /**
     * Genera informe de jugadores con logo (todos los jugadores)
     */
    @FXML
    private void generarInformeJugadoresConLogo() {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TituloInforme", "Listado Completo de Jugadores");
        parametros.put("CONDICION", "");
        parametros.put("EQUIPO_ID", getEquipoIdParaInformes());

        lanzaInforme("/reports/jugadores_filtrado_simple.jrxml", parametros);
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
        parametros.put("EQUIPO_ID", getEquipoIdParaInformes());

        lanzaInforme("/reports/jugadores_filtrado_simple.jrxml", parametros);
    }

    /**
     * Ejemplo: Genera informe de jugadores de la categoría Cadete
     */
    @FXML
    private void generarInformeCadete() {
        generarInformeFiltrado("cadete", "Jugadores Cadete");
    }

    /**
     * Ejemplo: Genera informe de jugadores de la categoría Infantil
     */
    @FXML
    private void generarInformeInfantil() {
        generarInformeFiltrado("infantil", "Jugadores Infantil");
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
     * Función genérica para lanzar informes JasperReports
     * Basada en la implementación del proyecto DI-T3-main
     * @param rutaInforme Ruta del archivo .jasper (ej: "/reports/informe.jasper")
     * @param parametros HashMap con parámetros del informe
     */
    private void lanzaInforme(String rutaInforme, Map<String, Object> parametros) {
        try {
            actualizarEstado("Generando informe...", "#FF9800");

            // Inyectar CLUB_ID del usuario en sesion
            SesionUsuario sesion = SesionUsuario.getInstance();
            if (sesion.haySesionActiva() && sesion.getUsuarioActual().getClubId() != null) {
                parametros.put("CLUB_ID", sesion.getUsuarioActual().getClubId());
            }

            // 1. CARGA o COMPILA el informe (.jasper precompilado o .jrxml en tiempo de ejecución)
            JasperReport report;
            if (rutaInforme.endsWith(".jrxml")) {
                report = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream(rutaInforme)
                );
            } else {
                report = (JasperReport) JRLoader.loadObject(
                    getClass().getResourceAsStream(rutaInforme)
                );
            }

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

                String tituloInforme = (String) parametros.getOrDefault("TituloInforme", "informe");
                String nombreArchivo = limpiarNombreArchivo(tituloInforme);

                String userHome = System.getProperty("user.home");
                String outputDir = userHome + File.separator + "Documents" + File.separator + "Scouteo";

                File directorioSalida = new File(outputDir);
                if (!directorioSalida.exists()) {
                    directorioSalida.mkdirs();
                }

                // Exportar a HTML y abrir en nueva ventana
                String outputHtmlFile = outputDir + File.separator + nombreArchivo + ".html";
                JasperExportManager.exportReportToHtmlFile(jasperPrint, outputHtmlFile);

                // Exportar a PDF
                String pdfOutputPath = outputDir + File.separator + nombreArchivo + ".pdf";
                JasperExportManager.exportReportToPdfFile(jasperPrint, pdfOutputPath);

                WebView wv = new WebView();
                wv.getEngine().load(new File(outputHtmlFile).toURI().toString());
                StackPane stackPane = new StackPane(wv);
                Scene scene = new Scene(stackPane, 950, 750);
                Stage stage = new Stage();
                stage.setTitle(tituloInforme + " — Scouteo");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(true);
                stage.setScene(scene);
                StageUtils.setAppIcon(stage);
                stage.show();
                actualizarEstado("Informe generado: " + nombreArchivo + ".pdf", "#4CAF50");
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
