import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * 남선공원 주차 관리 시스템의 GUI 인터페이스를 제공하는 클래스.
 * Swing 라이브러리를 사용하여 사용자 인터페이스를 구성하고,
 * {@link NamseonParkingSystem} 클래스와 상호작용하여 주차 관리 기능을 수행합니다.
 */
public class ParkingSystemGUI extends JFrame implements ActionListener {

    private NamseonParkingSystem parkingSystem;

    private JTextField carNumberField;
    private JComboBox<String> carTypeComboBox;
    private JButton parkButton;

    private JList<String> parkedCarList;
    private DefaultListModel<String> listModel;
    private JButton exitButton;

    private JTextArea statusArea;
    private JLabel messageLabel;
    private JLabel dailyStatsLabel;

    /**
     * ParkingSystemGUI 생성자.
     * 초기 주차 용량을 사용자로부터 입력받아 {@link NamseonParkingSystem} 객체를 생성하고,
     * GUI 컴포넌트를 초기화 및 배치한 후 화면에 표시합니다.
     */
    public ParkingSystemGUI() {
        int initialCapacity = 10;
        boolean capacitySet = false;
        while (!capacitySet) {
            try {
                String capacityInput = JOptionPane.showInputDialog(this, "주차장 최대 수용 차량 수를 입력하세요:", "초기 설정", JOptionPane.QUESTION_MESSAGE);
                if (capacityInput == null) {
                    System.exit(0);
                }
                initialCapacity = Integer.parseInt(capacityInput.trim());
                if (initialCapacity <= 0) {
                    JOptionPane.showMessageDialog(this, "주차 용량은 1 이상이어야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                } else {
                    capacitySet = true;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "유효한 숫자를 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        }

        parkingSystem = new NamseonParkingSystem(initialCapacity);

        setTitle("남선공원 주차 관리 시스템 (GUI)");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initComponents();

        updateParkingStatus();
        updateParkedCarList();
        updateDailyStats();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * GUI 컴포넌트를 초기화하고 프레임에 배치합니다.
     * 입차 패널, 출차 패널, 상태/메시지 패널, 통계/보고서 패널을 구성합니다.
     */
    private void initComponents() {
        JPanel parkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        parkPanel.setBorder(BorderFactory.createTitledBorder("차량 입차"));
        carNumberField = new JTextField(10);
        String[] carTypeDescriptions = new String[CarType.values().length];
        for (int i = 0; i < CarType.values().length; i++) {
            carTypeDescriptions[i] = CarType.values()[i].getDescription();
        }
        carTypeComboBox = new JComboBox<>(carTypeDescriptions);
        parkButton = new JButton("입차");
        parkButton.addActionListener(this);

        parkPanel.add(new JLabel("차량번호:"));
        parkPanel.add(carNumberField);
        parkPanel.add(new JLabel("유형:"));
        parkPanel.add(carTypeComboBox);
        parkPanel.add(parkButton);

        JPanel exitPanel = new JPanel(new BorderLayout(5, 5));
        exitPanel.setBorder(BorderFactory.createTitledBorder("주차 현황 및 출차"));
        listModel = new DefaultListModel<>();
        parkedCarList = new JList<>(listModel);
        parkedCarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(parkedCarList);
        exitButton = new JButton("선택 차량 출차");
        exitButton.addActionListener(this);

        exitPanel.add(scrollPane, BorderLayout.CENTER);
        exitPanel.add(exitButton, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        statusArea = new JTextArea(4, 20);
        statusArea.setEditable(false);
        statusArea.setBorder(BorderFactory.createTitledBorder("주차장 상태"));
        JScrollPane statusScrollPane = new JScrollPane(statusArea);

        messageLabel = new JLabel("시스템 준비 완료.", SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createTitledBorder("메시지"));
        messageLabel.setOpaque(true);
        messageLabel.setBackground(Color.LIGHT_GRAY);

        infoPanel.add(statusScrollPane, BorderLayout.CENTER);
        infoPanel.add(messageLabel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        dailyStatsLabel = new JLabel("입차: 0 | 출차: 0 | 매출: 0원", SwingConstants.LEFT);
        JButton reportButton = new JButton("매출 통계 보고서");
        reportButton.addActionListener(this);

        bottomPanel.add(dailyStatsLabel, BorderLayout.CENTER);
        bottomPanel.add(reportButton, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.add(exitPanel, BorderLayout.CENTER);
        mainContentPanel.add(infoPanel, BorderLayout.SOUTH);

        add(parkPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        parkPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                parkPanel.getBorder()));
        exitPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 5, 5, 5),
                exitPanel.getBorder()));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 5, 5, 5),
                infoPanel.getBorder()));
    }

    /**
     * GUI 컴포넌트의 액션 이벤트를 처리합니다.
     * 입차, 출차, 보고서 버튼 클릭 이벤트를 감지하고 해당 메서드를 호출합니다.
     * @param e 발생한 ActionEvent 객체
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == parkButton) {
            parkCarAction();
        } else if (e.getSource() == exitButton) {
            exitCarAction();
        } else if (e.getActionCommand().equals("매출 통계 보고서")) {
            generateAndShowReport();
        }
    }

    /**
     * '입차' 버튼 클릭 시 호출되는 액션 처리 메서드.
     * 입력된 차량 번호와 선택된 차량 유형으로 {@link NamseonParkingSystem#parkCar(String, CarType)}를 호출하고,
     * 결과를 메시지 레이블에 표시하며 관련 GUI 컴포넌트를 업데이트합니다.
     * 차량 번호가 입력되지 않은 경우 오류 메시지를 표시합니다.
     */
    private void parkCarAction() {
        String carNumber = carNumberField.getText().trim();
        if (carNumber.isEmpty()) {
            showMessage("차량 번호를 입력하세요.", true);
            return;
        }
        String selectedDescription = (String) carTypeComboBox.getSelectedItem();
        CarType selectedType = null;
        for (CarType type : CarType.values()) {
            if (type.getDescription().equals(selectedDescription)) {
                selectedType = type;
                break;
            }
        }

        if (selectedType == null) {
             showMessage("유효하지 않은 차량 유형입니다.", true);
             return;
        }

        boolean success = parkingSystem.parkCar(carNumber, selectedType);

        if (success) {
            showMessage("입차 완료: " + carNumber + " (" + selectedType.getDescription() + ")", false);
            carNumberField.setText("");
            updateParkingStatus();
            updateParkedCarList();
            updateDailyStats();
        } else {
             showMessage("입차 실패. (주차장이 만차이거나 내부 오류 발생)", true);
        }
    }

    /**
     * '선택 차량 출차' 버튼 클릭 시 호출되는 액션 처리 메서드.
     * 주차 현황 목록에서 선택된 차량의 인덱스를 기반으로 슬롯 번호를 계산하고,
     * {@link NamseonParkingSystem#exitCarBySlot(int)}를 호출하여 출차를 시도합니다.
     * 결과를 메시지 레이블에 표시하며 관련 GUI 컴포넌트를 업데이트합니다.
     * 목록에서 차량이 선택되지 않은 경우 오류 메시지를 표시합니다.
     */
    private void exitCarAction() {
        int selectedIndex = parkedCarList.getSelectedIndex();
        if (selectedIndex == -1) {
            showMessage("출차할 차량을 목록에서 선택하세요.", true);
            return;
        }
        int slotNumber = parkingSystem.getCurrentCount() - selectedIndex;

        boolean success = parkingSystem.exitCarBySlot(slotNumber);

        if (success) {
             showMessage("선택한 차량이 출차되었습니다.", false);
             updateParkingStatus();
             updateParkedCarList();
             updateDailyStats();
        } else {
             showMessage("출차 실패. (선택 오류 또는 내부 오류)", true);
        }
    }

    /**
     * 주차장 상태(최대 수용, 현재 주차, 남은 공간)를 업데이트하여 화면에 표시합니다.
     * {@link NamseonParkingSystem}의 getter 메서드를 사용합니다.
     */
    private void updateParkingStatus() {
        String statusInfo = "최대 수용: " + parkingSystem.getCapacity() + "대\n" +
                            "현재 주차: " + parkingSystem.getCurrentCount() + "대\n" +
                            "남은 공간: " + parkingSystem.getRemainingSpace() + "대";
        statusArea.setText(statusInfo);
    }

    /**
     * 당일 통계(입차 수, 출차 수, 총 매출)를 업데이트하여 화면 하단 레이블에 표시합니다.
     * {@link NamseonParkingSystem}의 getter 메서드를 사용합니다.
     */
    private void updateDailyStats() {
        String stats = String.format("입차: %d | 출차: %d | 매출: %,d원",
                                     parkingSystem.getDailyEntryCount(),
                                     parkingSystem.getDailyExitCount(),
                                     parkingSystem.getDailyTotalRevenue());
        dailyStatsLabel.setText(stats);
    }

    /**
     * 현재 주차된 차량 목록을 업데이트하여 화면의 JList에 표시합니다.
     * {@link NamseonParkingSystem#getParkedCarListInfo()}를 호출하여 차량 정보를 가져옵니다.
     */
    private void updateParkedCarList() {
        listModel.clear();
        List<String> parkedCars = parkingSystem.getParkedCarListInfo();
        for (String carInfo : parkedCars) {
            listModel.addElement(carInfo);
        }
    }

    /**
     * '매출 통계 보고서' 버튼 클릭 시 호출되는 메서드.
     * {@link NamseonParkingSystem}에서 입/출차 기록 및 매출 데이터를 가져와 보고서 내용을 생성하고,
     * 생성된 보고서를 새로운 다이얼로그 창에 표시합니다.
     */
    private void generateAndShowReport() {
        List<NamseonParkingSystem.ParkingRecord> entryRecords = parkingSystem.getEntryRecords();
        List<NamseonParkingSystem.ParkingRecord> exitRecords = parkingSystem.getExitRecords();
        int totalRevenue = parkingSystem.getDailyTotalRevenue();
        DateTimeFormatter reportFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("===== 남선공원 주차장 일일 매출 보고서 =====\n");
        reportContent.append("보고서 발행 시각: ").append(LocalDateTime.now().format(reportFormatter)).append("\n\n");

        reportContent.append("--- 시간대별 입차 기록 ---\n");
        if (entryRecords.isEmpty()) {
            reportContent.append("입차 기록이 없습니다.\n");
        } else {
            entryRecords.sort(Comparator.comparing(NamseonParkingSystem.ParkingRecord::getEntryTime));
            for (NamseonParkingSystem.ParkingRecord record : entryRecords) {
                reportContent.append(record.formatForReport(reportFormatter)).append("\n");
            }
        }
        reportContent.append("\n");

        reportContent.append("--- 시간대별 출차 기록 ---\n");
        if (exitRecords.isEmpty()) {
            reportContent.append("출차 기록이 없습니다.\n");
        } else {
            exitRecords.sort(Comparator.comparing(NamseonParkingSystem.ParkingRecord::getExitTime));
            for (NamseonParkingSystem.ParkingRecord record : exitRecords) {
                reportContent.append(record.formatForReport(reportFormatter)).append("\n");
            }
        }
        reportContent.append("\n");

        reportContent.append("--- 당일 총 매출 ---\n");
        reportContent.append(String.format("총 매출: %,d원\n", totalRevenue));
        reportContent.append("========================================");

        JTextArea reportTextArea = new JTextArea(reportContent.toString());
        reportTextArea.setEditable(false);
        reportTextArea.setLineWrap(true);
        reportTextArea.setWrapStyleWord(true);

        JScrollPane reportScrollPane = new JScrollPane(reportTextArea);
        reportScrollPane.setPreferredSize(new Dimension(500, 400));

        JDialog reportDialog = new JDialog(this, "일일 매출 보고서", true);
        reportDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        reportDialog.getContentPane().add(reportScrollPane);
        reportDialog.pack();
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setVisible(true);
    }

    /**
     * 메시지를 화면 하단 메시지 레이블에 표시합니다.
     * 오류 메시지인 경우 배경색을 변경하여 구분합니다.
     * @param message 표시할 메시지 문자열
     * @param isError 오류 메시지 여부 (true: 오류, false: 일반)
     */
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        if (isError) {
            messageLabel.setBackground(Color.PINK);
        } else {
            messageLabel.setBackground(Color.LIGHT_GRAY);
        }
    }

    /**
     * 애플리케이션의 메인 진입점.
     * Swing GUI를 이벤트 디스패치 스레드(EDT)에서 안전하게 실행합니다.
     * @param args 커맨드 라인 인수 (사용되지 않음)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ParkingSystemGUI();
            }
        });
    }
}