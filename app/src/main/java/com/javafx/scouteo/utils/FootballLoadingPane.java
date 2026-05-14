package com.javafx.scouteo.utils;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
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

public class FootballLoadingPane extends StackPane {

    private static final double W       = 300;
    private static final double H       = 82;
    private static final double BAR_X   = 10;
    private static final double BAR_W   = W - BAR_X * 2;
    private static final double BAR_Y   = 54;
    private static final double BAR_H   = 7;
    private static final double BALL_SZ = 28;
    private static final double BALL_R  = 13;

    private enum State { IDLE, LOADING, FINISHING }

    private State  state    = State.IDLE;
    private double progress = 0.0;
    private double prevProg = 0.0;
    private double spin     = 0.0;

    private final Canvas  canvas = new Canvas(W, H);
    private final Text    lblMsg = new Text("Cargando...");
    private       Timeline timeline;

    public FootballLoadingPane() {
        setStyle("-fx-background-color: rgba(13,27,42,0.80);");
        setMouseTransparent(false);
        setVisible(false);

        lblMsg.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        lblMsg.setFill(Color.web("#ffffffaa"));

        Text logoText = new Text("SCOUTEO");
        logoText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        logoText.setFill(Color.web("#C9A84C"));

        VBox card = new VBox(14, logoText, canvas, lblMsg);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: #0D1B2A;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 32 48 32 48;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 30, 0, 0, 8);" +
            "-fx-border-color: rgba(201,168,76,0.25);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 16;"
        );

        getChildren().add(card);
        buildTimeline();
        draw();
    }

    public void setMensaje(String msg) { lblMsg.setText(msg); }

    public void mostrar() {
        setVisible(true);
        setOpacity(1.0);
        progress = 0.0;
        prevProg = 0.0;
        spin     = 0.0;
        state    = State.LOADING;
        timeline.play();
    }

    // Fase 2: cuando la carga real termina, sprint al 100% y cierra
    public void ocultar() {
        if (!isVisible() || state == State.IDLE) return;
        state = State.FINISHING;
    }

    private void buildTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void tick() {
        prevProg = progress;

        switch (state) {
            case LOADING -> {
                // Rápido hasta 75%, se frena hasta 88%, crawl máximo al 92%
                if      (progress < 0.75) progress += 0.012;
                else if (progress < 0.88) progress += 0.002;
                else                      progress += 0.0002;
                progress = Math.min(progress, 0.92);
            }
            case FINISHING -> {
                // Sprint al 100% y cierra
                progress += 0.030;
                if (progress >= 1.0) {
                    progress = 1.0;
                    draw();
                    timeline.stop();
                    state = State.IDLE;
                    PauseTransition pausa = new PauseTransition(Duration.millis(280));
                    pausa.setOnFinished(ev -> fadeOut());
                    pausa.play();
                    return;
                }
            }
            default -> { return; }
        }

        double delta = progress - prevProg;
        spin += (delta * BAR_W / (2 * Math.PI * BALL_R)) * 360;
        draw();
    }

    private void fadeOut() {
        FadeTransition ft = new FadeTransition(Duration.millis(250), this);
        ft.setFromValue(getOpacity());
        ft.setToValue(0.0);
        ft.setOnFinished(e -> setVisible(false));
        ft.play();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, W, H);

        double ballCX = BAR_X + progress * BAR_W;
        double ballCY = BAR_Y - BALL_R - 1;

        drawBar(g, ballCX);
        drawShadow(g, ballCX);
        drawBall(g, ballCX, ballCY);
        drawPct(g);
    }

    private void drawBar(GraphicsContext g, double ballCX) {
        // Pista de fondo
        g.setFill(Color.web("#ffffff12"));
        g.fillRoundRect(BAR_X, BAR_Y, BAR_W, BAR_H, BAR_H, BAR_H);

        // Relleno dorado
        double fillW = Math.max(BAR_H, ballCX - BAR_X);
        g.setFill(Color.web("#C9A84C"));
        g.fillRoundRect(BAR_X, BAR_Y, fillW, BAR_H, BAR_H, BAR_H);

        // Brillo superior
        g.setFill(Color.web("#DEC06A55"));
        g.fillRoundRect(BAR_X + 2, BAR_Y + 1, Math.max(0, fillW - 4), BAR_H * 0.4, 2, 2);
    }

    private void drawShadow(GraphicsContext g, double ballCX) {
        g.setFill(Color.web("#00000050"));
        g.fillOval(ballCX - 12, BAR_Y + 2, 24, 5);
    }

    private void drawBall(GraphicsContext g, double ballCX, double ballCY) {
        g.save();
        g.translate(ballCX, ballCY);
        g.rotate(spin % 360);
        g.setFont(Font.font("Segoe UI Emoji", BALL_SZ));
        g.fillText("⚽", -BALL_SZ * 0.50, BALL_SZ * 0.42);
        g.restore();
    }

    private void drawPct(GraphicsContext g) {
        int pct = (int)(progress * 100);
        String txt = pct + "%";
        g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        g.setFill(Color.web("#C9A84C"));
        g.fillText(txt, W / 2 - txt.length() * 3.2, BAR_Y + BAR_H + 14);
    }
}
