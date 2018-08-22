package actions.mvphandler;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MVPHandler extends JDialog {
    private JPanel contentPane;
    private JTextArea textArea1;
    private JButton OKButton;
    private JButton cancelButton;
    private OnGenerateListener listener;

    public void setOnGenerateListener(OnGenerateListener listener) {
        this.listener = listener;
    }

    public MVPHandler() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(OKButton);

        OKButton.addActionListener(e -> listener.onGenerate(textArea1.getText().trim()));

        OKButton.addActionListener(e -> listener.onCancel());
        cancelButton.addActionListener(e -> listener.onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                listener.onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> listener.onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    public interface OnGenerateListener {
        void onGenerate(String text);

        void onCancel();
    }
}
