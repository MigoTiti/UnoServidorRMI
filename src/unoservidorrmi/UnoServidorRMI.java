package unoservidorrmi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import unoservidorrmi.rede.GerenciadorPartidas;

public class UnoServidorRMI extends JApplet {

    private static final int JFXPANEL_WIDTH_INT = 500;
    private static final int JFXPANEL_HEIGHT_INT = 500;
    private static JFXPanel fxContainer;

    private static TextArea logger;

    public static final int PORTA = 12345;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            }

            JFrame frame = new JFrame("Uno - Servidor RMI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JApplet applet = new UnoServidorRMI();
            applet.init();

            frame.setContentPane(applet.getContentPane());

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            applet.start();
        });
    }

    @Override
    public void init() {
        fxContainer = new JFXPanel();
        fxContainer.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, JFXPANEL_HEIGHT_INT));
        add(fxContainer, BorderLayout.CENTER);

        Platform.runLater(() -> {
            createScene();
        });
    }

    private static void createScene() {
        logger = new TextArea();
        logger.setEditable(false);

        StackPane root = new StackPane();
        root.setPadding(new Insets(30));
        root.getChildren().add(logger);
        fxContainer.setScene(new Scene(root));
        
        new Thread(() -> iniciarServidor()).start();
    }

    private static void iniciarServidor() {
        try {
            LocateRegistry.createRegistry(PORTA);
            Registry registro = LocateRegistry.getRegistry(PORTA);
            registro.rebind("GerenciadorPartidas", GerenciadorPartidas.getInstance());
            appendMensagem("Servidor iniciado");
        } catch (RemoteException ex) {
            exibirException(ex);
        }
    }
    
    public static void appendMensagem(String mensagem) {
        Platform.runLater(() -> {
            logger.appendText(mensagem + "\n");
        });
    }

    public static void exibirException(Exception ex) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Exception");
            alert.setContentText(ex.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        });
    }
}
