package com.javafx.scouteo.controller;

import com.google.gson.JsonObject;
import com.javafx.scouteo.dao.EquipoDAO;
import com.javafx.scouteo.dao.JugadorDAO;
import com.javafx.scouteo.dao.PartidoDAO;
import com.javafx.scouteo.model.Equipo;
import com.javafx.scouteo.model.Jugador;
import com.javafx.scouteo.model.Partido;
import com.javafx.scouteo.util.ApiClient;
import com.javafx.scouteo.util.SesionUsuario;
import com.javafx.scouteo.utils.StageUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperPrintManager;

import java.awt.Desktop;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

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
     * Genera informe de listado de jugadores2
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
        parametros.put("EQUIPO_ID", null);  // Siempre cargar todo el club para que el filtro por categoría funcione

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
        actualizarEstado("Generando informe...", "#FF9800");
        if (dashboardController != null) dashboardController.mostrarCargando("Generando informe...");

        Thread t = new Thread(() -> {
            try {
                SesionUsuario sesion = SesionUsuario.getInstance();
                if (sesion.haySesionActiva() && sesion.getUsuarioActual().getClubId() != null)
                    parametros.put("CLUB_ID", sesion.getUsuarioActual().getClubId());

                JasperReport report = rutaInforme.endsWith(".jrxml")
                        ? JasperCompileManager.compileReport(getClass().getResourceAsStream(rutaInforme))
                        : (JasperReport) JRLoader.loadObject(getClass().getResourceAsStream(rutaInforme));

                JRDataSource ds = construirDataSource(rutaInforme, parametros);
                if (ds == null) {
                    Platform.runLater(() -> {
                        if (dashboardController != null) dashboardController.ocultarCargando();
                        mostrarAlerta("Este informe requiere conexión directa a la BD.\nUsa el módulo Gráficos para ver estadísticas.");
                        actualizarEstado("Informe no disponible", "#FF9800");
                    });
                    return;
                }

                JasperPrint jasperPrint = JasperFillManager.fillReport(report, parametros, ds);

                if (!jasperPrint.getPages().isEmpty()) {
                    String titulo     = (String) parametros.getOrDefault("TituloInforme", "informe");
                    String nombreArch = limpiarNombreArchivo(titulo);
                    String outputDir  = com.javafx.scouteo.util.Preferencias.getPdfDirectory();
                    new File(outputDir).mkdirs();
                    String htmlPath = outputDir + File.separator + nombreArch + ".html";
                    String pdfPath  = outputDir + File.separator + nombreArch + ".pdf";
                    JasperExportManager.exportReportToHtmlFile(jasperPrint, htmlPath);
                    JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);

                    final JasperPrint jp = jasperPrint;
                    final File pdfFile   = new File(pdfPath);
                    Platform.runLater(() -> {
                        if (dashboardController != null) dashboardController.ocultarCargando();

                        WebView wv = new WebView();
                        wv.getEngine().load(new File(htmlPath).toURI().toString());
                        VBox.setVgrow(wv, Priority.ALWAYS);

                        Button btnImprimir = new Button("🖨 Imprimir");
                        btnImprimir.setOnAction(e -> {
                            try {
                                JasperPrintManager.printReport(jp, true);
                            } catch (Exception ex) {
                                mostrarAlerta("Error al imprimir: " + ex.getMessage());
                            }
                        });

                        Button btnAbrirPdf = new Button("📄 Abrir PDF");
                        btnAbrirPdf.setOnAction(e -> {
                            try {
                                Desktop.getDesktop().open(pdfFile);
                            } catch (Exception ex) {
                                mostrarAlerta("Error al abrir PDF: " + ex.getMessage());
                            }
                        });

                        HBox toolbar = new HBox(8, btnImprimir, btnAbrirPdf);
                        toolbar.setAlignment(Pos.CENTER_RIGHT);
                        toolbar.setPadding(new Insets(6, 12, 6, 12));
                        toolbar.setStyle("-fx-background-color: #ECEFF1; -fx-border-color: #CFD8DC; -fx-border-width: 0 0 1 0;");

                        VBox root = new VBox(toolbar, wv);
                        Stage stage = new Stage();
                        stage.setTitle(titulo + " — Scouteo");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setResizable(true);
                        stage.setScene(new Scene(root, 950, 750));
                        StageUtils.setAppIcon(stage);
                        stage.show();
                        if (com.javafx.scouteo.util.Preferencias.getAbrirPdfAuto()) {
                            try { Desktop.getDesktop().open(pdfFile); } catch (Exception ignored) {}
                        }
                        actualizarEstado("Informe generado: " + nombreArch + ".pdf", "#4CAF50");
                    });
                } else {
                    Platform.runLater(() -> {
                        if (dashboardController != null) dashboardController.ocultarCargando();
                        mostrarAlerta("La búsqueda no generó páginas");
                        actualizarEstado("Sin resultados", "#FF9800");
                    });
                }
            } catch (JRException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (dashboardController != null) dashboardController.ocultarCargando();
                    mostrarAlerta("Error Jasper: " + e.getMessage());
                    actualizarEstado("Error informe", "#F44336");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (dashboardController != null) dashboardController.ocultarCargando();
                    mostrarAlerta("Error inesperado: " + e.getMessage());
                    actualizarEstado("Error", "#F44336");
                });
            }
        }, "jasper-report");
        t.setDaemon(true);
        t.start();
    }

    private JRDataSource construirDataSource(String ruta, Map<String, Object> params) {
        Integer equipoId = (Integer) params.get("EQUIPO_ID");
        Integer clubId   = (Integer) params.get("CLUB_ID");
        String  condicion = (String)  params.getOrDefault("CONDICION", "");
        if (ruta.contains("Simple_Blue") || ruta.contains("jugadores_filtrado"))
            return dsJugadores(equipoId, clubId, condicion);
        if (ruta.contains("partidos"))
            return dsPartidos(equipoId, clubId);
        if (ruta.contains("estadisticas_completas")) return dsEstadisticas(equipoId, clubId);
        return null;
    }

    private JRDataSource dsJugadores(Integer equipoId, Integer clubId, String condicion) {
        JugadorDAO jugDAO = new JugadorDAO();
        EquipoDAO  eqDAO  = new EquipoDAO();
        List<Equipo> todosEquipos = eqDAO.obtenerPorClub(clubId != null ? clubId : 0);
        Map<Integer, Equipo> equipoMap = todosEquipos.stream()
                .collect(Collectors.toMap(Equipo::getId, e -> e, (a, b) -> a));

        List<Jugador> jugadores;
        if (equipoId != null && equipoId > 0) {
            jugadores = jugDAO.obtenerPorEquipo(equipoId);
        } else {
            // Fetch by equipo to bypass role-based API filtering
            jugadores = new ArrayList<>();
            for (Equipo eq : todosEquipos) {
                jugadores.addAll(jugDAO.obtenerPorEquipo(eq.getId()));
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Jugador j : jugadores) {
            if ("baja".equals(j.getEstado())) continue;
            Equipo eq = equipoMap.get(j.getEquipoId());
            if (condicion != null && !condicion.isEmpty()) {
                boolean posPk = j.getGrupoPosicion().equalsIgnoreCase(condicion)
                        || (j.getPosicion() != null && j.getPosicion().contains(condicion.toLowerCase()));
                boolean catOk = eq != null && condicion.equalsIgnoreCase(eq.getCategoria());
                if (!posPk && !catOk) continue;
            }
            long edad = 0;
            if (j.getFechaNacimiento() != null)
                edad = Period.between(j.getFechaNacimiento(), LocalDate.now()).getYears();
            Map<String, Object> row = new HashMap<>();
            row.put("dorsal",          j.getDorsal());
            row.put("jugador",         nvl(j.getNombre()) + " " + nvl(j.getApellidos()));
            row.put("posicion_label",  posLabel(j.getPosicion()));
            row.put("edad",            edad);
            row.put("altura_cm",       j.getAlturaCm());
            row.put("pie",             pieLabel(j.getPiernaDominante()));
            row.put("estado_raw",      j.getEstado());
            row.put("estado_label",    estadoLabel(j.getEstado()));
            row.put("equipo",          j.getNombreEquipo());
            row.put("categoria_label", eq != null ? catLabel(eq.getCategoria()) : "");
            row.put("temporada",       eq != null ? eq.getTemporada() : "");
            rows.add(row);
        }
        rows.sort((a, b) -> {
            int cmp = String.valueOf(a.getOrDefault("equipo", ""))
                           .compareTo(String.valueOf(b.getOrDefault("equipo", "")));
            if (cmp != 0) return cmp;
            int da = a.get("dorsal") instanceof Integer ? (Integer) a.get("dorsal") : 999;
            int db = b.get("dorsal") instanceof Integer ? (Integer) b.get("dorsal") : 999;
            return Integer.compare(da, db);
        });
        @SuppressWarnings("unchecked")
        java.util.Collection<Map<String, ?>> dsJug = (java.util.Collection<Map<String, ?>>) (java.util.Collection<?>) rows;
        return new JRMapCollectionDataSource(dsJug);
    }

    private JRDataSource dsPartidos(Integer equipoId, Integer clubId) {
        PartidoDAO pDAO = new PartidoDAO();
        EquipoDAO  eDAO = new EquipoDAO();
        List<Partido> partidos = (equipoId != null && equipoId > 0)
                ? pDAO.obtenerPorEquipo(equipoId) : pDAO.obtenerPorClub(clubId != null ? clubId : 0);
        Map<Integer, Equipo> eqMap = eDAO.obtenerPorClub(clubId != null ? clubId : 0)
                .stream().collect(Collectors.toMap(Equipo::getId, e -> e, (a, b) -> a));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Partido p : partidos) {
            Equipo eq = eqMap.get(p.getEquipoId());
            String resRaw = "pendiente", resLabel = "Pendiente";
            if (p.getGolesFavor() != null && p.getGolesContra() != null) {
                if      (p.getGolesFavor() > p.getGolesContra())        { resRaw = "victoria"; resLabel = "Victoria"; }
                else if (p.getGolesFavor().equals(p.getGolesContra()))  { resRaw = "empate";   resLabel = "Empate";   }
                else                                                      { resRaw = "derrota";  resLabel = "Derrota";  }
            }
            Map<String, Object> row = new HashMap<>();
            row.put("fecha",          p.getFechaHora() != null ? java.sql.Date.valueOf(p.getFechaHora().toLocalDate()) : null);
            row.put("rival",          p.getRival());
            row.put("tipo_label",     "local".equals(p.getTipo()) ? "Local" : "Visitante");
            row.put("goles_favor",    p.getGolesFavor());
            row.put("goles_contra",   p.getGolesContra());
            row.put("resultado_raw",  resRaw);
            row.put("resultado_label",resLabel);
            row.put("posesion",       p.getPosesionPct());
            row.put("tiros_puerta",   p.getTirosAPuerta());
            row.put("competicion",    p.getCompeticion());
            row.put("equipo",         eq != null ? eq.getNombre() : p.getNombreEquipo());
            row.put("temporada",      eq != null ? eq.getTemporada() : "");
            rows.add(row);
        }
        rows.sort((a, b) -> {
            java.sql.Date da = a.get("fecha") instanceof java.sql.Date ? (java.sql.Date) a.get("fecha") : new java.sql.Date(0);
            java.sql.Date db = b.get("fecha") instanceof java.sql.Date ? (java.sql.Date) b.get("fecha") : new java.sql.Date(0);
            return da.compareTo(db);
        });
        @SuppressWarnings("unchecked")
        java.util.Collection<Map<String, ?>> dsPar = (java.util.Collection<Map<String, ?>>) (java.util.Collection<?>) rows;
        return new JRMapCollectionDataSource(dsPar);
    }

    private JRDataSource dsEstadisticas(Integer equipoId, Integer clubId) {
        JugadorDAO jugDAO = new JugadorDAO();
        PartidoDAO pDAO   = new PartidoDAO();
        EquipoDAO  eDAO   = new EquipoDAO();

        List<Jugador> jugadores = (equipoId != null && equipoId > 0)
                ? jugDAO.obtenerPorEquipo(equipoId) : jugDAO.obtenerPorClub(clubId != null ? clubId : 0);
        List<Partido> partidos = (equipoId != null && equipoId > 0)
                ? pDAO.obtenerPorEquipo(equipoId) : pDAO.obtenerPorClub(clubId != null ? clubId : 0);
        Map<Integer, Equipo>  eqMap  = eDAO.obtenerPorClub(clubId != null ? clubId : 0)
                .stream().collect(Collectors.toMap(Equipo::getId, e -> e, (a, b) -> a));
        Map<Integer, Jugador> jugMap = jugadores.stream()
                .collect(Collectors.toMap(Jugador::getId, j -> j, (a, b) -> a));

        // int[] = [partidos, minutos, goles, asistencias, amarillas, rojas, valoracion*100 sum, valoracion count]
        ConcurrentHashMap<Integer, int[]> agg = new ConcurrentHashMap<>();
        ApiClient api = ApiClient.getInstance();

        ExecutorService exec = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();
        for (Partido p : partidos) {
            final int pid = p.getId();
            futures.add(exec.submit(() -> {
                String json = api.get("/alineaciones/partido/" + pid);
                if (json == null) return;
                for (JsonObject o : api.fromJsonList(json, JsonObject.class)) {
                    if (!o.has("jugadorId") || o.get("jugadorId").isJsonNull()) continue;
                    int jid = o.get("jugadorId").getAsInt();
                    int mins = 0, goles = 0, asist = 0, amarillas = 0, rojas = 0, vSum = 0, vCnt = 0;
                    if (o.has("minutoSalida") && !o.get("minutoSalida").isJsonNull())
                        mins = o.get("minutoSalida").getAsInt();
                    else if (o.has("minutosJugados") && !o.get("minutosJugados").isJsonNull())
                        mins = o.get("minutosJugados").getAsInt();
                    if (o.has("goles") && !o.get("goles").isJsonNull()) goles = o.get("goles").getAsInt();
                    if (o.has("asistencias") && !o.get("asistencias").isJsonNull()) asist = o.get("asistencias").getAsInt();
                    if (o.has("tarjetasAmarillas") && !o.get("tarjetasAmarillas").isJsonNull()) amarillas = o.get("tarjetasAmarillas").getAsInt();
                    if (o.has("tarjetasRojas") && !o.get("tarjetasRojas").isJsonNull()) rojas = o.get("tarjetasRojas").getAsInt();
                    if (o.has("valoracion") && !o.get("valoracion").isJsonNull()) {
                        vSum = (int) (o.get("valoracion").getAsDouble() * 100);
                        vCnt = 1;
                    }
                    final int[] inc = {1, mins, goles, asist, amarillas, rojas, vSum, vCnt};
                    agg.merge(jid, inc, (ex, in) -> new int[]{
                        ex[0]+in[0], ex[1]+in[1], ex[2]+in[2], ex[3]+in[3],
                        ex[4]+in[4], ex[5]+in[5], ex[6]+in[6], ex[7]+in[7]});
                }
            }));
        }
        exec.shutdown();
        try { exec.awaitTermination(30, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<Integer, int[]> entry : agg.entrySet()) {
            int jid = entry.getKey();
            int[] v = entry.getValue();
            Jugador j = jugMap.get(jid);
            if (j == null) continue;
            Equipo eq = eqMap.get(j.getEquipoId());
            long pj = v[0];
            BigDecimal totalGoles = BigDecimal.valueOf(v[2]);
            BigDecimal golesPorPartido = pj > 0
                    ? totalGoles.divide(BigDecimal.valueOf(pj), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal promVal = v[7] > 0
                    ? BigDecimal.valueOf(v[6]).divide(BigDecimal.valueOf((long) v[7] * 100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            Map<String, Object> row = new HashMap<>();
            row.put("jugador",            nvl(j.getNombre()) + " " + nvl(j.getApellidos()));
            row.put("equipo",             eq != null ? eq.getNombre() : nvl(j.getNombreEquipo()));
            row.put("posicion_abbr",      j.getGrupoPosicion());
            row.put("partidos_jugados",   pj);
            row.put("minutos_total",      BigDecimal.valueOf(v[1]));
            row.put("total_goles",        totalGoles);
            row.put("total_asistencias",  BigDecimal.valueOf(v[3]));
            row.put("total_amarillas",    BigDecimal.valueOf(v[4]));
            row.put("total_rojas",        BigDecimal.valueOf(v[5]));
            row.put("goles_por_partido",  golesPorPartido);
            row.put("prom_valoracion",    promVal);
            row.put("estado_raw",         j.getEstado());
            row.put("estado_label",       estadoLabel(j.getEstado()));
            rows.add(row);
        }
        rows.sort((a, b) -> {
            int cmp = String.valueOf(a.getOrDefault("equipo", ""))
                           .compareTo(String.valueOf(b.getOrDefault("equipo", "")));
            if (cmp != 0) return cmp;
            return String.valueOf(a.getOrDefault("jugador", ""))
                         .compareTo(String.valueOf(b.getOrDefault("jugador", "")));
        });
        @SuppressWarnings("unchecked")
        Collection<Map<String, ?>> dsEst = (Collection<Map<String, ?>>) (Collection<?>) rows;
        return new JRMapCollectionDataSource(dsEst);
    }

    private static String nvl(String s)  { return s != null ? s : ""; }

    private String posLabel(String p) {
        if (p == null) return "";
        switch (p) {
            case "portero":           return "Portero";
            case "defensa_central":   return "Def. Central";
            case "lateral_derecho":   return "Lateral Der.";
            case "lateral_izquierdo": return "Lateral Izq.";
            case "mediocentro":       return "Mediocentro";
            case "medio_derecho":     return "Medio Der.";
            case "medio_izquierdo":   return "Medio Izq.";
            case "mediapunta":        return "Mediapunta";
            case "extremo_derecho":   return "Extremo Der.";
            case "extremo_izquierdo": return "Extremo Izq.";
            case "delantero_centro":  return "Delantero";
            default: return p;
        }
    }

    private String pieLabel(String p) {
        if ("izquierda".equals(p)) return "Izquierda";
        if ("derecha".equals(p))   return "Derecha";
        return "Ambidiestro";
    }

    private String estadoLabel(String e) {
        if ("lesionado".equals(e))  return "Lesionado";
        if ("sancionado".equals(e)) return "Sancionado";
        return "Activo";
    }

    private String catLabel(String c) {
        if (c == null) return "";
        switch (c) {
            case "prebenjamin":  return "Prebenjamin";
            case "benjamin":     return "Benjamin";
            case "alevin":       return "Alevin";
            case "infantil":     return "Infantil";
            case "cadete":       return "Cadete";
            case "juvenil":      return "Juvenil";
            case "primer_equipo":return "Primer Equipo";
            case "senior":       return "Senior";
            case "veteranos":    return "Veteranos";
            default: return c;
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
