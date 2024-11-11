import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MyFrame extends JFrame {

    private JRadioButton[] elevator, eButton, fButton;
    private JLabel[] elevatorStatus; // Array to store text next to each elevator
    private JLabel inactiveElevatorsLabel, activeElevatorsLabel, realTimeLabel;
    private ArrayList<Integer> inactiveElevatorsList;
    private int activeElevator = -1;
    private Timer elevatorTimer;
    private boolean elevatorInTransit = false;
    private int[] targetFloors; // Array to store target floors for each elevator
    private int[] elevatorFloors; // Array to track current floors of the elevators

    MyFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Elevator Control Panel");
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null); // Center the frame on the screen
        this.getContentPane().setBackground(Color.green);

        elevator = new JRadioButton[8];
        eButton = new JRadioButton[20];
        fButton = new JRadioButton[20];
        elevatorStatus = new JLabel[8]; // Initialize text views next to elevators
        targetFloors = new int[8]; // Initialize the target floors array
        elevatorFloors = new int[8]; // Initialize the current floor of each elevator
        inactiveElevatorsList = new ArrayList<>();

        // Initialize inactive elevators list (1-8)
        for (int i = 1; i <= 8; i++) {
            inactiveElevatorsList.add(i);
            targetFloors[i - 1] = -1; // Set default target floor as -1
            elevatorFloors[i - 1] = i - 1; // Start elevators at corresponding floor (0 to 7 for now)
        }

        // First column: Elevator buttons
        JPanel elevatorPanel = new JPanel();
        elevatorPanel.setLayout(new BoxLayout(elevatorPanel, BoxLayout.Y_AXIS));
        elevatorPanel.setBorder(BorderFactory.createTitledBorder("Elevators"));

        for (int i = 0; i < elevator.length; i++) {
            elevator[i] = new JRadioButton("Elevator " + (i + 1));
            elevator[i].setEnabled(false); // Disable elevators initially
            elevator[i].addActionListener(new RadioButtonListener());

            // Create and add small text view next to each elevator
            JPanel elevatorWithStatus = new JPanel();
            elevatorWithStatus.setLayout(new FlowLayout());
            elevatorWithStatus.add(elevator[i]);
            elevatorStatus[i] = new JLabel("Floor " + elevatorFloors[i] + " - Waiting...");
            elevatorWithStatus.add(elevatorStatus[i]);
            elevatorPanel.add(elevatorWithStatus);
        }

        // Second column: eButtons (split into two columns)
        JPanel eButtonPanel = new JPanel(new GridLayout(1, 2));
        eButtonPanel.setBorder(BorderFactory.createTitledBorder("Select Destination Floor"));

        JPanel eButtonPanel1 = new JPanel();
        eButtonPanel1.setLayout(new BoxLayout(eButtonPanel1, BoxLayout.Y_AXIS));

        JPanel eButtonPanel2 = new JPanel();
        eButtonPanel2.setLayout(new BoxLayout(eButtonPanel2, BoxLayout.Y_AXIS));

        for (int i = 0; i < eButton.length; i++) {
            eButton[i] = new JRadioButton("eFloor " + (i + 1));
            eButton[i].setEnabled(false); // Initially disable eButtons
            eButton[i].addActionListener(new RadioButtonListener());
            if (i < 10) {
                eButtonPanel1.add(eButton[i]);
            } else {
                eButtonPanel2.add(eButton[i]);
            }
        }

        eButtonPanel.add(eButtonPanel1);
        eButtonPanel.add(eButtonPanel2);

        // Third column: fButtons (split into two columns)
        JPanel fButtonPanel = new JPanel(new GridLayout(1, 2));
        fButtonPanel.setBorder(BorderFactory.createTitledBorder("Request an Elevator"));

        JPanel fButtonPanel1 = new JPanel();
        fButtonPanel1.setLayout(new BoxLayout(fButtonPanel1, BoxLayout.Y_AXIS));

        JPanel fButtonPanel2 = new JPanel();
        fButtonPanel2.setLayout(new BoxLayout(fButtonPanel2, BoxLayout.Y_AXIS));

        for (int i = 0; i < fButton.length; i++) {
            fButton[i] = new JRadioButton("Floor " + (i + 1));
            fButton[i].addActionListener(new RadioButtonListener());
            if (i < 10) {
                fButtonPanel1.add(fButton[i]);
            } else {
                fButtonPanel2.add(fButton[i]);
            }
        }

        fButtonPanel.add(fButtonPanel1);
        fButtonPanel.add(fButtonPanel2);

        // Group the radio buttons
        ButtonGroup elevators = new ButtonGroup();
        ButtonGroup elevatorButtons = new ButtonGroup();
        ButtonGroup floorButtons = new ButtonGroup();

        for (JRadioButton button : elevator) {
            elevators.add(button);
        }
        for (JRadioButton button : eButton) {
            elevatorButtons.add(button);
        }
        for (JRadioButton button : fButton) {
            floorButtons.add(button);
        }

        // Text views
        inactiveElevatorsLabel = new JLabel("Inactive Elevators: " + inactiveElevatorsList);
        activeElevatorsLabel = new JLabel("Active Elevator: None");
        realTimeLabel = new JLabel("Real Time Status: Request an Elevator");

        // Main panel to hold all columns and text views
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.add(elevatorPanel);
        mainPanel.add(eButtonPanel);
        mainPanel.add(fButtonPanel);

        // Add panels and labels to the frame
        JPanel textPanel = new JPanel(new GridLayout(3, 1));
        textPanel.add(inactiveElevatorsLabel);
        textPanel.add(activeElevatorsLabel);
        textPanel.add(realTimeLabel);

        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(textPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private class RadioButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JRadioButton source = (JRadioButton) e.getSource();

            // fButton selection logic
            if (source.getText().contains("Floor")) {
                handleFButtonSelection(source);
            }
            // eButton selection logic (after elevator is active)
            else if (source.getText().contains("eFloor")) {
                handleEButtonSelection(source);
            }
            // Elevator selection logic
            else if (source.getText().contains("Elevator")) {
                handleElevatorSelection(source);
            }
        }
    }

    private void handleFButtonSelection(JRadioButton source) {
        if (!elevatorInTransit) {
            selectRandomElevator(source);  // Select a random elevator if no elevator is in transit.
            elevatorInTransit = true;
            for (JRadioButton button : eButton) {
                button.setEnabled(true);
            }

        } else {
            selectNewElevator(source); // If an elevator is already in transit, select another one.

        }
    }

    private void handleEButtonSelection(JRadioButton source) {
        if (elevatorInTransit && activeElevator != -1) {
            // If an elevator is in transit and active, move the elevator to the floor selected.
            int selectedFloor = Integer.parseInt(source.getText().split(" ")[1]) - 1; // Get the floor from the eButton label.
            targetFloors[activeElevator - 1] = selectedFloor;
            elevatorStatus[activeElevator - 1].setText("Elevator " + activeElevator + " - Heading to Floor " + targetFloors[activeElevator - 1]);
            selectDestinationFloor(source);
        }
    }

    private void handleElevatorSelection(JRadioButton source) {
        for (int i = 0; i < elevator.length; i++) {
            elevator[i].setEnabled(false); // Disable all other elevators
        }
        realTimeLabel.setText("Real Time Status: " + source.getText() + " Select The Destination Floor");
    }

    private void selectRandomElevator(JRadioButton source) {
        if (!inactiveElevatorsList.isEmpty()) {
            Random rand = new Random();
            activeElevator = inactiveElevatorsList.remove(rand.nextInt(inactiveElevatorsList.size()));
            elevator[activeElevator - 1].setSelected(true); // Mark the elevator as selected
            int selectedFloor = Integer.parseInt(source.getText().split(" ")[1]) - 1; // Get the floor from the eButton label.
            targetFloors[activeElevator - 1] = selectedFloor;
            elevatorStatus[activeElevator - 1].setText("Current Floor " + elevatorFloors[activeElevator - 1] + " - Heading to Floor " + ((targetFloors[activeElevator - 1]) + 1));
            activeElevatorsLabel.setText("Active Elevator: " + activeElevator);
            elevatorTimer = new Timer();
            elevatorTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    elevatorFloors[activeElevator - 1] = targetFloors[activeElevator - 1];
                    elevatorStatus[activeElevator - 1].setText("Elevator " + activeElevator + " - Arrived at Floor " + ((elevatorFloors[activeElevator - 1]) + 1 ) + ". Select Destination Floor");

                    resetElevatorState();
                }
            }, 5000);
        }
    }

    private void selectDestinationFloor(JRadioButton source) {
            int elevatorId = Integer.parseInt(source.getText().split(" ")[1]);
            int selectedFloor = Integer.parseInt(source.getText().split(" ")[1]) - 1;
            targetFloors[activeElevator - 1] = selectedFloor;
            elevatorFloors[elevatorId - 1] = targetFloors[elevatorId - 1];
            elevatorStatus[elevatorId - 1].setText( "Heading to Floor " + targetFloors[elevatorId - 1]);
            elevatorTimer = new Timer();
            elevatorTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    elevatorFloors[activeElevator - 1] = targetFloors[activeElevator - 1];
                    elevatorStatus[activeElevator - 1].setText(" Arrived on Floor " + ((elevatorFloors[activeElevator - 1]) + 1 ));

                    resetElevatorState();
                }
            }, 5000);
    }

    private void selectNewElevator(JRadioButton source) {
        if (inactiveElevatorsList.size() > 0) {
            int elevatorId = Integer.parseInt(source.getText().split(" ")[1]);
            targetFloors[elevatorId - 1] = Integer.parseInt(source.getText().split(" ")[1]); // Set new target
            elevatorFloors[elevatorId - 1] = targetFloors[elevatorId - 1];
            elevatorStatus[elevatorId - 1].setText( "Heading to Floor " + targetFloors[elevatorId - 1]);
            elevatorTimer = new Timer();
            elevatorTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    elevatorFloors[activeElevator - 1] = targetFloors[activeElevator - 1];
                    elevatorStatus[activeElevator - 1].setText("Current Floor " + elevatorFloors[activeElevator - 1] + " - Heading to Floor " + ((targetFloors[activeElevator - 1]) + 1));

                    resetElevatorState();
                }
            }, 5000);
        }
    }

    private void startArrivalCountdown() {
        elevatorTimer = new Timer();
        elevatorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                elevatorFloors[activeElevator - 1] = targetFloors[activeElevator - 1];
                elevatorStatus[activeElevator - 1].setText("Elevator " + activeElevator + " - Arrived at Floor " + ((elevatorFloors[activeElevator - 1]) + 1 ) + ". Select Destination Floor");

                resetElevatorState();
            }
        }, 5000); // Countdown for 5 seconds before arrival
    }

    private void resetElevatorState() {
        elevatorInTransit = false;
        elevator[activeElevator - 1].setEnabled(true); // Re-enable elevator for next use
        inactiveElevatorsList.add(activeElevator); // Re-add elevator back to inactive list
        activeElevator = -1;
        activeElevatorsLabel.setText("Active Elevator: None");
    }

    public static void main(String[] args) {
        new MyFrame();
    }
}
