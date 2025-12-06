package frc.robot;

import static edu.wpi.first.units.Units.MetersPerSecond;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;
import frc.robot.generated.DragonConfigs;
import frc.robot.generated.RubyConfigs;
import frc.robot.subsystems.CommandSwerveDrivetrain;

public class Utils {
    public enum RobotName {
        NEO,
        SONIC,
        DRAGON,
        RUBY,
        NONE//not in use right now maybe later
        }

    public static RobotName getRobotName() {
        System.out.println("Robot Serial Number: "+RobotController.getSerialNumber());
        // if(RobotBase.isSimulation())
        //     return RobotName.NEO;
        // else if("0327B986".equals(RobotController.getSerialNumber()))
        //     return RobotName.SONIC;
        // else if("0318860e".equals(RobotController.getSerialNumber())) 
        //     return RobotName.NEO; // test bench
        if ("034159C7".equals(RobotController.getSerialNumber())) 
            return RobotName.DRAGON;
        else 
            return RobotName.RUBY; 
    }

    public static CommandSwerveDrivetrain createDrivetrainAuto(RobotName name){
        if(name == RobotName.RUBY){
            System.out.println("Creating Ruby Drivetrain");
            return RubyConfigs.createDrivetrain();
        }
        else if(name == RobotName.DRAGON){
            System.out.println("Creating dragon Drivetrain");
            return DragonConfigs.createDrivetrain();
        }
        else{
            System.out.println("error finding/parsing robot ID defaulting to ruby");
            return RubyConfigs.createDrivetrain();
        }
    }

    public static double setMaxSpeedAuto(RobotName name){
        if(name == RobotName.RUBY){
            System.out.println("Creating Ruby Drivetrain");
            return RubyConfigs.kSpeedAt12Volts.in(MetersPerSecond);
        }
        else if(name == RobotName.DRAGON){
            System.out.println("Creating dragon Drivetrain");
            return DragonConfigs.kSpeedAt12Volts.in(MetersPerSecond);
        }
        else{
            System.out.println("error finding/parsing robot ID defaulting to ruby");
            return RubyConfigs.kSpeedAt12Volts.in(MetersPerSecond);
        }
    }

}
