package particle.editor;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ParticleSystemModel;
import javax.swing.*;
import java.io.*;

public class ConfigIO {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void save(ParticleSystemModel model, File file) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter()
                .writeValue(file, model);
    }

    public static ParticleSystemModel load(File file) throws IOException {
        return MAPPER.readValue(file, ParticleSystemModel.class);
    }

    public static void installMenu(JMenuBar bar,
                                   ParticleSystemModel model,
                                   Runnable onReload) {
        JMenu file = new JMenu("File");
        JMenuItem save = new JMenuItem("Save...");
        JMenuItem open = new JMenuItem("Open...");
        file.add(save);
        file.add(open);
        bar.add(file);

        save.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try { save(model, fc.getSelectedFile()); }
                catch (IOException ex) { ex.printStackTrace(); }
            }
        });
        open.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    ParticleSystemModel m2 = load(fc.getSelectedFile());
                    onReload.run(); // 外部で新モデルを置き換える
                } catch (IOException ex) { ex.printStackTrace(); }
            }
        });
    }
}