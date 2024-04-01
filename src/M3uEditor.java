import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class M3uEditor extends JFrame {
    private JTextField prefixField;
    private JList<String> fileList;
    private DefaultListModel<String> listModel;


    public M3uEditor() {
        setTitle("M3U Editor V1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 300));
        ImageIcon icon = new ImageIcon(getClass().getResource("/ico.png"));
        setIconImage(icon.getImage());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openMenuItem = new JMenuItem("Open File");
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        fileMenu.add(openMenuItem);
        menuBar.add(fileMenu);

        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
        menuBar.add(aboutMenuItem);

        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建前缀输入框
        JPanel prefixPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prefixPanel.add(new JLabel("Prefix:"));
        prefixField = new JTextField(20);
        prefixPanel.add(prefixField);
        mainPanel.add(prefixPanel, BorderLayout.NORTH);

        // 创建文件列表
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setDragEnabled(true);
        fileList.setDropMode(DropMode.INSERT);
        fileList.setTransferHandler(new FileTransferHandler());
        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelectedItems();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(fileList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedItems();
            }
        });
        buttonPanel.add(deleteButton);

        JButton moveUpButton = new JButton("↑");
        moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedItemUp();
            }
        });
        buttonPanel.add(moveUpButton);

        JButton moveDownButton = new JButton("↓");
        moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedItemDown();
            }
        });
        buttonPanel.add(moveDownButton);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFileList();
            }
        });
        buttonPanel.add(saveButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open File");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(fileToOpen))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    listModel.addElement(line);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAboutDialog() {
        String message = "<html><body>"
            + "M3U Editor V1.0<br>"
            + "Developed by Haden Lee<br>"
            + "Website: <a href=\"https://www.hadenlee.com\">https://www.hadenlee.com</a>"
            + "</body></html>";

        JLabel label = new JLabel(message);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    String url = "https://www.hadenlee.com";
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JOptionPane.showMessageDialog(this, label, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedItems() {
        int[] selectedIndices = fileList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            listModel.remove(selectedIndices[i]);
        }
    }

    private void moveSelectedItemUp() {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex > 0) {
            String item = listModel.remove(selectedIndex);
            listModel.add(selectedIndex - 1, item);
            fileList.setSelectedIndex(selectedIndex - 1);
        }
    }

    private void moveSelectedItemDown() {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < listModel.size() - 1) {
            String item = listModel.remove(selectedIndex);
            listModel.add(selectedIndex + 1, item);
            fileList.setSelectedIndex(selectedIndex + 1);
        }
    }

    private void saveFileList() {
        String prefix = prefixField.getText();
        List<String> filePathList = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            filePathList.add(listModel.get(i));
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File List");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                for (String filePath : filePathList) {
                    writer.write(prefix + filePath);
                    writer.newLine();
                }
                JOptionPane.showMessageDialog(this, "File list saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file list: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class FileTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            Transferable transferable = support.getTransferable();
            try {
                List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                for (File file : files) {
                    String path = file.getAbsolutePath();
                    path = path.substring(2);
                    listModel.addElement(path);
                }
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new M3uEditor().setVisible(true);
            }
        });
    }

}
