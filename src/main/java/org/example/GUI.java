package org.example;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import java.util.stream.Collectors;

public class GUI {
    private JFrame frame;
    private JList<String> KPIList;
    private JList<String> sehirList;
    private JList<String> baslangicTarihiList;
    private JList<String> bitisTarihiList;
    private JButton verileriGosterButonu;
    private JTextArea sonucTextArea;
    private JCheckBox multiSelectCheckBox;
    private ChartManager chartManager;
    private DataManager dataManager;
    private String dosyaYolu;
    private Set<Date> tumTarihlerSet = new HashSet<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private JTextField aramaTextField;
    private JTextField sehirTextField;
    private JComboBox<String> grafikSecimComboBox;

    public GUI(String dosyaYolu) {
        dataManager = new DataManager();
        chartManager = new ChartManager();
        this.dosyaYolu = dosyaYolu;
        initializeUI();
        loadTarihler();
    }

    private void initializeUI() {
        setLookAndFeel();

        frame = new JFrame("Veri Seçici");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = createMainPanel(dosyaYolu);
        frame.add(mainPanel, BorderLayout.CENTER);

        JPanel chartPanelContainer = new JPanel(new BorderLayout());
        frame.add(chartPanelContainer, BorderLayout.SOUTH);
        chartManager.setChartPanelContainer(chartPanelContainer);

        frame.setVisible(true);
    }


    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private JPanel createMainPanel(String dosyaYolu) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = createGridBagConstraints();
        addComponentsToPanel(mainPanel, dosyaYolu, gbc);
        mainPanel.setBackground(new Color(143,188,143));
        return mainPanel;
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        return gbc;
    }

    private void addComponentsToPanel(JPanel panel, String dosyaYolu, GridBagConstraints gbc) {
        aramaTextField = new JTextField(10);
        aramaTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterKPIList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterKPIList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterKPIList();
            }
        });

        sehirTextField = new JTextField(10);
        sehirTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterSehirList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterSehirList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterSehirList();
            }
        });

        KPIList = new JList<>(dataManager.getSortedUniqueIsimlerArray());
        KPIList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sehirList = new JList<>(dataManager.getSortedUniqueSehirlerArray());

        verileriGosterButonu = createShowDataButton();
        sonucTextArea = createResultTextArea();

        multiSelectCheckBox = new JCheckBox("Çoklu Seçimi Aktif Et");
        multiSelectCheckBox.setFont(new Font("Arial",Font.BOLD,12));
        multiSelectCheckBox.addActionListener(e -> toggleSelectionMode());

        baslangicTarihiList = new JList<>(dataManager.getSortedTarihlerArray());
        baslangicTarihiList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        baslangicTarihiList.addListSelectionListener(e -> updateBitisTarihiList());
        bitisTarihiList = new JList<>(dataManager.getSortedTarihlerArray());
        bitisTarihiList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JLabel baslangicTarihiLabel = new JLabel("Başlangıç Tarihi:");
        baslangicTarihiLabel.setFont(new Font("Arial",Font.BOLD,12));
        JLabel bitisTarihiLabel = new JLabel("Bitiş Tarihi:");
        bitisTarihiLabel.setFont(new Font("Arial",Font.BOLD,12));
        JLabel kpiadiLabel=new JLabel("KPI Adı:");
        kpiadiLabel.setFont(new Font("Arial",Font.BOLD,12));
        JLabel sehirLabel=new JLabel("Şehir:");
        sehirLabel.setFont(new Font("Arial",Font.BOLD,12));

        grafikSecimComboBox = new JComboBox<>(new String[]{"Çubuk Grafik", "Pasta Grafik"});
        grafikSecimComboBox.setSelectedIndex(0);
        grafikSecimComboBox.addActionListener(e -> verileriGoster());

        panel.setLayout(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(kpiadiLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(aramaTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        panel.add(new JScrollPane(KPIList), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(multiSelectCheckBox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(sehirLabel, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx=15;
        panel.add(sehirTextField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(sehirList), gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(baslangicTarihiLabel, gbc);

        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(bitisTarihiLabel, gbc);

        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(baslangicTarihiList), gbc);

        gbc.gridx = 5;
        gbc.gridy = 1;
        panel.add(new JScrollPane(bitisTarihiList), gbc);

        gbc.gridx = 5;
        gbc.gridy = 15;
        gbc.gridwidth = -5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(grafikSecimComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(verileriGosterButonu, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 6;
        gbc.weighty = 2.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(sonucTextArea), gbc);
    }

    private void toggleSelectionMode() {
        if (multiSelectCheckBox.isSelected()) {
            KPIList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            KPIList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    private JButton createShowDataButton() {
        JButton button = new JButton("Verileri Göster");
        button.setBackground(new Color(0, 100, 0));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.addActionListener(e -> verileriGoster());
        return button;
    }

    private JTextArea createResultTextArea() {
        JTextArea textArea = new JTextArea(5, 40);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.white);
        return textArea;
    }

    private void verileriGoster() {
        List<String> seciliKPI = KPIList.getSelectedValuesList();
        String seciliSehir =  sehirList.getSelectedValue();
        String baslangicTarihiStr = baslangicTarihiList.getSelectedValue();
        String bitisTarihiStr = bitisTarihiList.getSelectedValue();

        if (seciliKPI.isEmpty() || seciliSehir == null || baslangicTarihiStr == null || bitisTarihiStr == null) {
            JOptionPane.showMessageDialog(frame, "Lütfen tüm seçimleri yapınız.");
            return;
        }

        List<String> veriler = dataManager.getVeriler(seciliKPI, seciliSehir, baslangicTarihiStr, bitisTarihiStr);

        if (veriler.isEmpty()) {
            sonucTextArea.setText("Seçimlerinize uygun veri bulunamadı.");
        } else {
            sonucTextArea.setText(String.join("\n", veriler));
        }

        Map<String, Double> averages = dataManager.getOperatorAverages(seciliKPI, seciliSehir, baslangicTarihiStr, bitisTarihiStr);
        boolean isPieChart = grafikSecimComboBox.getSelectedIndex() == 1;
        chartManager.updateChart(averages, isPieChart);
    }

    private void loadTarihler() {
        tumTarihlerSet = Arrays.stream(dataManager.getSortedTarihlerArray())
                .map(this::parseDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Date parseDate(String tarihStr) {
        try {
            return sdf.parse(tarihStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateBitisTarihiList() {
        if (tumTarihlerSet == null) {
            return;
        }
        String baslangicTarihiStr = baslangicTarihiList.getSelectedValue();
        if (baslangicTarihiStr != null) {
            Date baslangicTarihi = parseDate(baslangicTarihiStr);
            if (baslangicTarihi != null) {
                List<String> bitisTarihler = tumTarihlerSet.stream()
                        .filter(tarih -> tarih != null && tarih.after(baslangicTarihi))
                        .sorted()
                        .map(sdf::format)
                        .collect(Collectors.toList());
                bitisTarihiList.setListData(bitisTarihler.toArray(new String[0]));
            }
        }
    }

    private void filterKPIList() {
        String searchText = aramaTextField.getText().toLowerCase();
        List<String> filteredKPIs = Arrays.stream(dataManager.getSortedUniqueIsimlerArray())
                .filter(kpi -> kpi.toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        KPIList.setListData(filteredKPIs.toArray(new String[0]));
    }

    private void filterSehirList() {
        String searchText = sehirTextField.getText().toLowerCase();
        List<String> filteredSehirler = Arrays.stream(dataManager.getSortedUniqueSehirlerArray())
                .filter(sehir -> sehir.toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        sehirList.setListData(filteredSehirler.toArray(new String[0]));
    }
}
