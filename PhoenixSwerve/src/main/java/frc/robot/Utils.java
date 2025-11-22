package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;
import frc.robot.generated.DragonConfigs;
import frc.robot.generated.RubyConfigs;
import frc.robot.subsystems.CommandSwerveDrivetrain;

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

    public static CommandSwerveDrivetrain createDrivetrainAuto(String ID){
        if(ID.equals("ruby")){
            System.out.println("Creating Ruby Drivetrain");
            return RubyConfigs.createDrivetrain();
        }
        else if(ID.equals("waverunner")){
            System.out.println("Creating Waverunner Drivetrain");
            return DragonConfigs.createDrivetrain();
        }
        else
            System.out.println("error finding/parsing robot ID defaulting to ruby");
        
        return RubyConfigs.createDrivetrain();
    }
}
