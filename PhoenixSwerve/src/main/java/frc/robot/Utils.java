package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;

public class Utils {
    public static String getRobotId() {
        System.out.println("Robot Serial Number: "+RobotController.getSerialNumber());
        if(RobotBase.isSimulation())
            return "neo";
        else if("0327B986".equals(RobotController.getSerialNumber()))
            return "sonic";
        else if("0318860e".equals(RobotController.getSerialNumber())) {
            return "neo"; // test bench
        } 
        else if ("0327B986".equals(RobotController.getSerialNumber())) {
            return "sonic";
        } 
        else if ("034159C7".equals(RobotController.getSerialNumber())) {
            return "waverunner";
        }
        else {
            return "ruby";
        }
    }

}
