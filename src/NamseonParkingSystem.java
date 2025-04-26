import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 차량 유형을 나타내는 Enum.
 * 각 유형별 시간당 요금과 설명을 포함합니다.
 */
enum CarType {
    GENERAL(1000, "일반"),
    COMPACT(500, "경차"),
    DISABLED(0, "장애인"),
    OFFICIAL(0, "공무");

    private final int hourlyRate;
    private final String description;

    /**
     * CarType Enum 생성자.
     * @param hourlyRate 시간당 요금
     * @param description 차량 유형 설명
     */
    CarType(int hourlyRate, String description) {
        this.hourlyRate = hourlyRate;
        this.description = description;
    }

    /**
     * 시간당 요금을 반환합니다.
     * @return 시간당 요금
     */
    public int getHourlyRate() {
        return hourlyRate;
    }

    /**
     * 차량 유형 설명을 반환합니다.
     * @return 차량 유형 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 숫자 코드를 기반으로 해당하는 CarType Enum 상수를 반환합니다.
     * 유효하지 않은 코드의 경우 기본값(GENERAL)을 반환합니다.
     * @param typeCode 차량 유형 숫자 코드 (1: 일반, 2: 경차, 3: 장애인, 4: 공무)
     * @return 해당하는 CarType Enum 상수
     */
    public static CarType fromTypeCode(int typeCode) {
        switch (typeCode) {
            case 1: return GENERAL;
            case 2: return COMPACT;
            case 3: return DISABLED;
            case 4: return OFFICIAL;
            default:
                return GENERAL;
        }
    }
}

/**
 * GUI 메뉴 옵션을 나타내는 Enum.
 * 각 메뉴 항목에 해당하는 숫자 값을 가집니다.
 */
enum MenuOption {
    PARK_CAR(1),
    EXIT_CAR(2),
    DISPLAY_CARS(3),
    DISPLAY_STATUS(4),
    EXIT_SYSTEM(0),
    INVALID(-1);

    private final int value;

    /**
     * MenuOption Enum 생성자.
     * @param value 메뉴 옵션 숫자 값
     */
    MenuOption(int value) {
        this.value = value;
    }

    /**
     * 메뉴 옵션 숫자 값을 반환합니다.
     * @return 메뉴 옵션 숫자 값
     */
    public int getValue() {
        return value;
    }

    /**
     * 숫자 값을 기반으로 해당하는 MenuOption Enum 상수를 반환합니다.
     * 유효하지 않은 값의 경우 INVALID를 반환합니다.
     * @param value 메뉴 옵션 숫자 값
     * @return 해당하는 MenuOption Enum 상수
     */
    public static MenuOption fromInt(int value) {
        for (MenuOption option : MenuOption.values()) {
            if (option.value == value) {
                return option;
            }
        }
        return INVALID;
    }
}

/**
 * 남선공원 주차장 관리 시스템의 핵심 로직을 담당하는 클래스.
 * 스택을 이용하여 주차 차량을 관리하고, 입/출차 이력 및 매출 통계를 기록합니다.
 */
public class NamseonParkingSystem {

    private Stack<Car> parkingLot;
    private Stack<Car> tempStack;

    private List<ParkingRecord> entryRecords;
    private List<ParkingRecord> exitRecords;
    private int dailyTotalRevenue;
    private int dailyEntryCount;
    private int dailyExitCount;

    private int capacity;
    private int currentCount;
    private DateTimeFormatter formatter;

    /**
     * NamseonParkingSystem 생성자.
     * 주차장 용량을 설정하고 관련 변수들을 초기화합니다.
     * @param capacity 주차장 최대 수용 가능 차량 수
     */
    public NamseonParkingSystem(int capacity) {
        this.parkingLot = new Stack<>();
        this.tempStack = new Stack<>();
        this.capacity = capacity;
        this.currentCount = 0;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.entryRecords = new ArrayList<>();
        this.exitRecords = new ArrayList<>();
        this.dailyTotalRevenue = 0;
        this.dailyEntryCount = 0;
        this.dailyExitCount = 0;
    }

    /**
     * 차량을 주차장에 입차시킵니다.
     * 주차 공간이 없으면 입차에 실패합니다.
     * 성공 시 입차 기록을 추가하고 당일 입차 수를 증가시킵니다.
     * @param carNumber 입차할 차량 번호
     * @param carType 입차할 차량 유형 (CarType Enum)
     * @return 입차 성공 시 true, 실패 시 false
     */
    public boolean parkCar(String carNumber, CarType carType) {
        if (currentCount >= capacity) {
            return false;
        }

        LocalDateTime entryTime = LocalDateTime.now();
        Car car = new Car(carNumber, carType, entryTime);
        parkingLot.push(car);
        currentCount++;

        ParkingRecord record = new ParkingRecord(car.getCarNumber(), car.getCarType(), car.getEntryTime(), null, 0, 0, "entry");
        entryRecords.add(record);
        dailyEntryCount++;

        return true;
    }

    /**
     * 지정된 슬롯 번호의 차량을 출차시킵니다.
     * 스택 구조 상 해당 슬롯까지 차량을 임시 스택으로 옮긴 후 처리합니다.
     * 성공 시 출차 기록을 추가하고 당일 출차 수 및 매출을 업데이트합니다.
     * @param slotNumber 출차할 차량의 슬롯 번호 (1부터 시작, 가장 최근 입차 차량이 1번)
     * @return 출차 성공 시 true, 실패 시 false
     */
    public boolean exitCarBySlot(int slotNumber) {
        if (parkingLot.isEmpty()) {
            return false;
        }
        if (slotNumber <= 0 || slotNumber > currentCount) {
            return false;
        }

        boolean found = false;
        Car targetCar = null;
        int currentSlot = 1;

        while (!parkingLot.isEmpty()) {
            Car car = parkingLot.pop();
            if (currentSlot == slotNumber) {
                targetCar = car;
                found = true;
                break;
            }
            tempStack.push(car);
            currentSlot++;
        }

        while (!tempStack.isEmpty()) {
            parkingLot.push(tempStack.pop());
        }

        if (found && targetCar != null) {
            LocalDateTime exitTime = LocalDateTime.now();
            Duration duration = Duration.between(targetCar.getEntryTime(), exitTime);
            long minutes = duration.toMinutes();
            long hours = (minutes / 60) + (minutes % 60 > 0 ? 1 : 0);
            int hourlyRate = targetCar.getCarType().getHourlyRate();
            int fee = (int) (hourlyRate * hours);

            currentCount--;

            ParkingRecord record = new ParkingRecord(targetCar.getCarNumber(), targetCar.getCarType(), targetCar.getEntryTime(), exitTime, hours, fee, "exit");
            exitRecords.add(record);
            dailyExitCount++;
            dailyTotalRevenue += fee;

            return true;
        } else {
            return false;
        }
    }

    /**
     * 현재 주차된 차량 목록 정보를 GUI 표시에 적합한 문자열 리스트로 반환합니다.
     * 리스트는 입차 시간이 오래된 순서대로 정렬됩니다.
     * @return 포맷팅된 차량 정보 문자열 리스트 (예: "12가3456 (일반) - 입차: yyyy-MM-dd HH:mm:ss")
     */
    public List<String> getParkedCarListInfo() {
        List<String> carInfoList = new ArrayList<>();
        Stack<Car> tempViewStack = (Stack<Car>) parkingLot.clone();
        Stack<Car> reverseStack = new Stack<>();

        while (!tempViewStack.isEmpty()) {
            reverseStack.push(tempViewStack.pop());
        }

        while (!reverseStack.isEmpty()) {
            Car car = reverseStack.pop();
            carInfoList.add(car.getCarNumber() + " (" + car.getCarType().getDescription() + ") - 입차: " + car.getEntryTime().format(formatter));
        }
        return carInfoList;
    }

    /**
     * 주차장의 최대 수용 가능 차량 수를 반환합니다.
     * @return 최대 수용 가능 차량 수
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 현재 주차된 차량 수를 반환합니다.
     * @return 현재 주차된 차량 수
     */
    public int getCurrentCount() {
        return currentCount;
    }

    /**
     * 현재 주차 가능한 남은 공간 수를 반환합니다.
     * @return 남은 주차 공간 수
     */
    public int getRemainingSpace() {
        return capacity - currentCount;
    }

    /**
     * 날짜 및 시간 포맷터를 반환합니다.
     * @return DateTimeFormatter 객체
     */
    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    /**
     * 당일 총 입차 차량 수를 반환합니다.
     * @return 당일 총 입차 수
     */
    public int getDailyEntryCount() {
        return dailyEntryCount;
    }

    /**
     * 당일 총 출차 차량 수를 반환합니다.
     * @return 당일 총 출차 수
     */
    public int getDailyExitCount() {
        return dailyExitCount;
    }

    /**
     * 당일 총 매출액을 반환합니다.
     * @return 당일 총 매출액
     */
    public int getDailyTotalRevenue() {
        return dailyTotalRevenue;
    }

    /**
     * 기록된 모든 입차 기록 리스트의 복사본을 반환합니다.
     * @return 입차 기록 리스트 (ParkingRecord 객체 리스트)
     */
    public List<ParkingRecord> getEntryRecords() {
        return new ArrayList<>(entryRecords);
    }

    /**
     * 기록된 모든 출차 기록 리스트의 복사본을 반환합니다.
     * @return 출차 기록 리스트 (ParkingRecord 객체 리스트)
     */
    public List<ParkingRecord> getExitRecords() {
        return new ArrayList<>(exitRecords);
    }


    /**
     * 주차된 차량 정보를 나타내는 내부 클래스.
     */
    public static class Car {
        private String carNumber;
        private CarType carType;
        private LocalDateTime entryTime;

        /**
         * Car 객체 생성자.
         * @param carNumber 차량 번호
         * @param carType 차량 유형 (CarType Enum)
         * @param entryTime 입차 시간
         */
        public Car(String carNumber, CarType carType, LocalDateTime entryTime) {
            this.carNumber = carNumber;
            this.carType = carType;
            this.entryTime = entryTime;
        }

        /**
         * 차량 번호를 반환합니다.
         * @return 차량 번호
         */
        public String getCarNumber() {
            return carNumber;
        }

        /**
         * 차량 유형(CarType Enum)을 반환합니다.
         * @return 차량 유형
         */
        public CarType getCarType() {
            return carType;
        }

        /**
         * 입차 시간을 반환합니다.
         * @return 입차 시간 (LocalDateTime)
         */
        public LocalDateTime getEntryTime() {
            return entryTime;
        }
    }

    /**
     * 입/출차 기록 정보를 나타내는 내부 클래스.
     */
    public static class ParkingRecord {
        private String carNumber;
        private CarType carType;
        private LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private long parkingHours;
        private int fee;
        private String recordType;

        /**
         * ParkingRecord 객체 생성자.
         * @param carNumber 차량 번호
         * @param carType 차량 유형
         * @param entryTime 입차 시간
         * @param exitTime 출차 시간 (입차 기록 시 null)
         * @param parkingHours 주차 시간 (시간 단위, 입차 기록 시 0)
         * @param fee 주차 요금 (입차 기록 시 0)
         * @param recordType 기록 유형 ("entry" 또는 "exit")
         */
        public ParkingRecord(String carNumber, CarType carType, LocalDateTime entryTime,
                             LocalDateTime exitTime, long parkingHours, int fee, String recordType) {
            this.carNumber = carNumber;
            this.carType = carType;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
            this.parkingHours = parkingHours;
            this.fee = fee;
            this.recordType = recordType;
        }

        /** 차량 번호를 반환합니다. @return 차량 번호 */
        public String getCarNumber() { return carNumber; }
        /** 차량 유형을 반환합니다. @return 차량 유형 (CarType Enum) */
        public CarType getCarType() { return carType; }
        /** 입차 시간을 반환합니다. @return 입차 시간 (LocalDateTime) */
        public LocalDateTime getEntryTime() { return entryTime; }
        /** 출차 시간을 반환합니다. (입차 기록 시 null) @return 출차 시간 (LocalDateTime) */
        public LocalDateTime getExitTime() { return exitTime; }
        /** 주차 시간을 반환합니다. (입차 기록 시 0) @return 주차 시간 (시간 단위) */
        public long getParkingHours() { return parkingHours; }
        /** 주차 요금을 반환합니다. (입차 기록 시 0) @return 주차 요금 */
        public int getFee() { return fee; }
        /** 기록 유형("entry" 또는 "exit")을 반환합니다. @return 기록 유형 */
        public String getRecordType() { return recordType; }

        /**
         * 보고서용으로 포맷팅된 문자열을 반환합니다.
         * @param formatter 사용할 DateTimeFormatter
         * @return 포맷팅된 보고서 문자열 라인
         */
        public String formatForReport(DateTimeFormatter formatter) {
            if ("entry".equals(recordType)) {
                return String.format("입차 | 시간: %s | 차량번호: %s (%s)",
                        entryTime.format(formatter), carNumber, carType.getDescription());
            } else {
                return String.format("출차 | 시간: %s | 차량번호: %s (%s) | 주차시간: %d시간 | 요금: %d원",
                        exitTime.format(formatter), carNumber, carType.getDescription(), parkingHours, fee);
            }
        }

        /**
         * 객체의 기본 문자열 표현을 반환합니다. (HH:mm:ss 형식 사용)
         * @return 포맷팅된 문자열
         */
        @Override
        public String toString() {
            return formatForReport(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }
}