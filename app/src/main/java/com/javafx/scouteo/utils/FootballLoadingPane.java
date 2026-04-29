package com.javafx.scouteo.utils;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Overlay de carga con balón ⚽ siguiendo un arco Bézier hacia portería.
 */
public class FootballLoadingPane extends StackPane {

    private static final double W      = 450;
    private static final double H      = 195;
    private static final double BALL_R = 17;
    private static final double GOAL_X = W - 74;
    private static final double GOAL_Y = H / 2.0 - 33;
    private static final double GOAL_W = 52;
    private static final double GOAL_H = 45;
    private static final double START_X = 24;
    private static final double FLOOR_Y = H * 0.64;

    // Puntos de control del arco Bézier cúbico
    private static final double CP1_X = START_X + W * 0.22;
    private static final double CP1_Y = FLOOR_Y - 70;
    private static final double CP2_X = GOAL_X - 30;
    private static final double CP2_Y = GOAL_Y - 6;
    private static final double END_X  = GOAL_X + GOAL_W * 0.5;
    private static final double END_Y  = GOAL_Y + GOAL_H * 0.5;

    // Estado de animación
    private double  t          = 0;
    private double  spin       = 0;
    private double  ballX      = START_X;
    private double  ballY      = FLOOR_Y;
    private boolean scoring    = false;
    private double  scorePause = 0;

    private final Canvas   canvas  = new Canvas(W, H);
    private final Text     lblMsg  = new Text("Cargando...");
    private       Timeline timeline;

    public FootballLoadingPane() {
        setStyle("-fx-background-color: rgba(0,0,0,0.62);");
        setMouseTransparent(false);
        setVisible(false);

        lblMsg.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 14));
        lblMsg.setFill(Color.WHITE);

        VBox box = new VBox(10, canvas, lblMsg);
        box.setAlignment(Pos.CENTER);
        box.setStyle(
            "-fx-background-color: rgba(10,36,10,0.96);" +
            "-fx-background-radius: 18;" +
            "-fx-padding: 22 38 22 38;" +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.7),24,0,0,6);"
        );
        getChildren().add(box);
        buildTimeline();
        draw();
    }

    // ── API pública ──────────────────────────────────────────────────────────

    public void setMensaje(String msg) {
        lblMsg.setText("⚽  " + msg);
    }

    public void mostrar() {
        setVisible(true);
        setOpacity(1.0);
        reset();
        timeline.play();
    }

    public void ocultar() {
        if (!isVisible()) return;   // idempotente
        timeline.stop();
        FadeTransition ft = new FadeTransition(Duration.millis(300), this);
        ft.setFromValue(getOpacity());
        ft.setToValue(0.0);
        ft.setOnFinished(e -> setVisible(false));
        ft.play();
    }

    // ── Animación ────────────────────────────────────────────────────────────

    private void buildTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void tick() {
        if (scoring) {
            scorePause++;
            if (scorePause > 55) reset();
        } else {
            // Ease-in: arranca lento, acelera progresivamente
            double speed = 0.007 + t * 0.019;
            t = Math.min(t + speed, 1.0);
            spin += 7 + t * 24;

            double[] pos = bezier(t);
            ballX = pos[0];
            ballY = pos[1];

            if (t >= 1.0) { scoring = true; scorePause = 0; }
        }
        draw();
    }

    private void reset() {
        t = 0; spin = 0; scoring = false; scorePause = 0;
        ballX = START_X; ballY = FLOOR_Y;
    }

    /** Bézier cúbico: posición en el instante t ∈ [0,1] */
    private double[] bezier(double t) {
        double u = 1 - t;
        double x = u*u*u * START_X + 3*u*u*t * CP1_X + 3*u*t*t * CP2_X + t*t*t * END_X;
        double y = u*u*u * FLOOR_Y  + 3*u*u*t * CP1_Y + 3*u*t*t * CP2_Y + t*t*t * END_Y;
        return new double[]{x, y};
    }

    // ── Dibujado ─────────────────────────────────────────────────────────────

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, W, H);
        drawField(g);
        drawGoal(g);
        drawShadow(g);
        if (!scoring) drawTrajectory(g);
        drawBall(g);
        if (scoring && scorePause > 6) drawCelebration(g);
    }

    private void drawField(GraphicsContext g) {
        g.setFill(Color.web("#1b4d1b"));
        g.fillRoundRect(0, 0, W, H, 14, 14);

        // Franjas de hierba alternadas
        for (int i = 0; i < (int)(W / 30) + 1; i++) {
            g.setFill(i % 2 == 0 ? Color.web("#ffffff07") : Color.web("#00000012"));
            g.fillRect(i * 30, 0, 30, H);
        }

        // Líneas del campo
        g.setStroke(Color.web("#ffffff2a"));
        g.setLineWidth(1.5);
        g.strokeLine(W / 2.0, 0, W / 2.0, H);
        g.strokeOval(W / 2.0 - 30, H / 2.0 - 30, 60, 60);
        g.strokeRect(GOAL_X - 22, H / 2.0 - 50, 22 + GOAL_W + 24, 100);

        // Línea de suelo (hierba)
        g.setStroke(Color.web("#2a6e2a"));
        g.setLineWidth(1.0);
        g.strokeLine(0, FLOOR_Y + 14, W, FLOOR_Y + 14);
    }

    private void drawGoal(GraphicsContext g) {
        double gx = GOAL_X, gy = GOAL_Y, gw = GOAL_W, gh = GOAL_H;
        double postW = 8.0, barH = 8.0;

        // ── Interior oscuro (profundidad de la red) ──────────────────────────
        g.setFill(Color.web("#040d04"));
        g.fillRect(gx, gy, gw, gh);

        // Flash dorado cuando hay gol
        if (scoring) {
            g.setFill(Color.web("#ffff0030"));
            g.fillRect(gx, gy, gw, gh);
        }

        // ── Red: cuadrícula blanca translúcida ────────────────────────────────
        g.setStroke(Color.web("#ffffff55"));
        g.setLineWidth(0.85);
        int cols = 6, rows = 5;
        for (int i = 0; i <= cols; i++) {
            double nx = gx + i * (gw / cols);
            g.strokeLine(nx, gy, nx, gy + gh);
        }
        for (int j = 0; j <= rows; j++) {
            double ny = gy + j * (gh / rows);
            g.strokeLine(gx, ny, gx + gw, ny);
        }

        // ── Poste izquierdo (tubo cilíndrico 3D) ──────────────────────────────
        // Sombra detrás del poste
        g.setFill(Color.web("#00000080"));
        g.fillRoundRect(gx - postW * 0.5 + 3.5, gy - 1, postW, gh + 2, 5, 5);
        // Cuerpo principal blanco
        g.setFill(Color.WHITE);
        g.fillRoundRect(gx - postW * 0.5, gy - 1, postW, gh + 2, 5, 5);
        // Franja de brillo (simula tubo redondo)
        g.setFill(Color.web("#ffffffd0"));
        g.fillRoundRect(gx - postW * 0.5 + 1.5, gy + 2, 2.5, gh - 4, 2, 2);

        // ── Poste derecho ──────────────────────────────────────────────────────
        g.setFill(Color.web("#00000080"));
        g.fillRoundRect(gx + gw - postW * 0.5 + 3.5, gy - 1, postW, gh + 2, 5, 5);
        g.setFill(Color.WHITE);
        g.fillRoundRect(gx + gw - postW * 0.5, gy - 1, postW, gh + 2, 5, 5);
        g.setFill(Color.web("#ffffffd0"));
        g.fillRoundRect(gx + gw - postW * 0.5 + 1.5, gy + 2, 2.5, gh - 4, 2, 2);

        // ── Larguero (barra horizontal) ────────────────────────────────────────
        // Sombra debajo del larguero
        g.setFill(Color.web("#00000080"));
        g.fillRoundRect(gx - postW * 0.5, gy - barH * 0.5 + 3.5, gw + postW, barH, 5, 5);
        // Cuerpo blanco
        g.setFill(Color.WHITE);
        g.fillRoundRect(gx - postW * 0.5, gy - barH * 0.5, gw + postW, barH, 5, 5);
        // Brillo superior del larguero
        g.setFill(Color.web("#ffffffd0"));
        g.fillRoundRect(gx + 2, gy - barH * 0.5 + 1.5, gw - 4, 2.5, 2, 2);

        // ── Línea de gol (suelo) ───────────────────────────────────────────────
        g.setStroke(Color.web("#ffffffcc"));
        g.setLineWidth(2.0);
        g.strokeLine(gx - postW * 0.4, gy + gh + 4, gx + gw + postW * 0.4, gy + gh + 4);
    }

    private void drawShadow(GraphicsContext g) {
        double height = FLOOR_Y - ballY;
        double scale  = Math.max(0.25, 1.0 - height / (FLOOR_Y * 0.85));
        double sw = BALL_R * 2.6 * scale;
        double sh = BALL_R * 0.42 * scale;
        g.setFill(Color.web("#00000050"));
        g.fillOval(ballX - sw / 2, FLOOR_Y + 10, sw, sh);
    }

    private void drawTrajectory(GraphicsContext g) {
        g.setStroke(Color.web("#ffffff38"));
        g.setLineWidth(1.0);
        g.setLineDashes(4.0, 6.0);
        g.strokeLine(ballX, ballY, END_X, END_Y);
        g.setLineDashes();
    }

    private void drawBall(GraphicsContext g) {
        double size = BALL_R * 2.5;
        g.save();
        g.translate(ballX, ballY);
        // Balanceo natural que simula la rotación del balón
        g.rotate(Math.sin(Math.toRadians(spin * 0.85)) * 15.0);
        g.setFont(Font.font("Segoe UI Emoji", size));
        // Centrar el emoji alrededor del punto de traducción
        g.fillText("⚽", -size * 0.50, size * 0.46);
        g.restore();
    }

    private void drawCelebration(GraphicsContext g) {
        double alpha = Math.min(1.0, (scorePause - 6) / 14.0);

        // Flash dorado de fondo
        g.setGlobalAlpha(alpha * 0.20);
        g.setFill(Color.YELLOW);
        g.fillRoundRect(0, 0, W, H, 14, 14);
        g.setGlobalAlpha(alpha);

        // Texto GOOOL con sombra
        g.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 26));
        g.setFill(Color.web("#00000095"));
        g.fillText("⚽  ¡GOOOL!  🎉", GOAL_X - 102 + 2, GOAL_Y - 6 + 2);
        g.setFill(Color.YELLOW);
        g.fillText("⚽  ¡GOOOL!  🎉", GOAL_X - 102, GOAL_Y - 6);

        // Chispas que ascienden
        g.setFont(Font.font("Segoe UI Emoji", 16));
        String[] sparks = {"✨", "⭐", "✨", "🌟"};
        double[] ex = {GOAL_X - 12, GOAL_X + GOAL_W * 0.5, GOAL_X + GOAL_W + 6, GOAL_X + GOAL_W * 0.25};
        double[] ey = {GOAL_Y - 16, GOAL_Y - 20, GOAL_Y - 14, GOAL_Y - 24};
        double rise = (scorePause - 6) * 0.65;
        for (int i = 0; i < sparks.length; i++) {
            g.fillText(sparks[i], ex[i], ey[i] - rise);
        }
        g.setGlobalAlpha(1.0);
    }
}
