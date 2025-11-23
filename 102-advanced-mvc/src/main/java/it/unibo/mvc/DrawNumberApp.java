package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        final Configuration.Builder config = new Configuration.Builder();
        final InputStream inputConfig = ClassLoader.getSystemResourceAsStream("config.yml");
        try (BufferedReader read = new BufferedReader(new InputStreamReader(inputConfig))) {
            String s;
            while ((s = read.readLine()) != null) {
                String[] split = s.split(":");
                switch (split[0]) {
                    case "minimum" -> config.setMin(Integer.parseInt(split[1].trim()));
                    case "maximum" -> config.setMax(Integer.parseInt(split[1].trim()));
                    case "attempts" -> config.setAttempts(Integer.parseInt(split[1].trim()));
                    default -> Arrays.stream(views).forEach(v -> v.displayError("Cannot read from file"));
                }
            }
        } catch (IOException | NumberFormatException e) {
            Arrays.stream(views).forEach(v -> v.displayError(e.getMessage()));
        }

        this.model = new DrawNumberImpl(config.build());
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(),
                          new DrawNumberViewImpl());
    }

}
