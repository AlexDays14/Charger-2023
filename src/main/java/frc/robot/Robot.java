// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
/* import edu.wpi.first.wpilibj.DoubleSolenoid; */
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;



public class Robot extends TimedRobot {

  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private Timer timer = new Timer();

  /* ---------- CONTROLLERS DECLARATION ---------- */

  WPI_VictorSPX _Drive_Left_Main = new WPI_VictorSPX(Constants.LEFT_MAIN_MOTOR);
  WPI_VictorSPX _Drive_Left_Follower = new WPI_VictorSPX(Constants.LEFT_FOLLOWER_MOTOR);

  WPI_VictorSPX _Drive_Right_Main = new WPI_VictorSPX(Constants.RIGHT_MAIN_MOTOR);
  WPI_VictorSPX _Drive_Right_Follower = new WPI_VictorSPX(Constants.RIGHT_FOLLOWER_MOTOR);

  TalonSRX _Arm = new TalonSRX(Constants.ARM_MOTOR);

  /* ---------- XBOX DECLARATION ---------- */

  XboxController _xboxDriver = new XboxController(0);
  XboxController _xboxOp = new XboxController(1);

  /* ---------- DIFFERENTIAL DRIVE ---------- */

  double forward = 0;
  double turn = 0;
  double arm = 0;
  double multVelocidad = .1;
  double multArm = .1;

  DifferentialDrive ADrive = new DifferentialDrive(_Drive_Left_Main, _Drive_Right_Main);

  /* ---------- PNEUMATICS ---------- */

  private final Compressor comp = new Compressor(PneumaticsModuleType.CTREPCM);
  private final Solenoid solenoid = new Solenoid(PneumaticsModuleType.CTREPCM, 3);
  /* private final DoubleSolenoid dbsolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 1, 2); */

  /**
  * This function is run when the robot is first started up and should be used for any
  * initialization code.
  */

@Override
public void robotInit() {
  CameraServer.startAutomaticCapture(0);
  m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
  m_chooser.addOption("My Auto", kCustomAuto);
  SmartDashboard.putData("Auto choices", m_chooser);

  /* Establecer que todos los motores esten en 0s al inicio */
  _Drive_Left_Main.set(ControlMode.PercentOutput, 0);
  _Drive_Left_Follower.set(ControlMode.PercentOutput, 0);
  _Drive_Right_Main.set(ControlMode.PercentOutput, 0);
  _Drive_Right_Follower.set(ControlMode.PercentOutput, 0);
  _Arm.set(ControlMode.PercentOutput, 0);

  _Drive_Left_Main.setInverted(true);
  _Drive_Left_Follower.setInverted(true);
  _Drive_Right_Main.setInverted(false);
  _Drive_Right_Follower.setInverted(false);
  _Arm.setInverted(true);

  _Drive_Left_Follower.follow(_Drive_Left_Main);
  _Drive_Right_Follower.follow(_Drive_Right_Main);

  _Arm.setNeutralMode(NeutralMode.Brake);
}

  /**
  * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
  * that you want ran during disabled, autonomous, teleoperated and test.
  *
  * <p>This runs after the mode specific periodic functions, but before LiveWindow and
  * SmartDashboard integrated updating.
  */

@Override
public void robotPeriodic() {
  _Arm.setNeutralMode(NeutralMode.Coast);
  _Arm.set(ControlMode.Position, arm);

  ADrive.arcadeDrive(forward * multVelocidad, -turn * multVelocidad);
}

  /**
  * This autonomous (along with the chooser code above) shows how to select between different
  * autonomous modes using the dashboard. The sendable chooser code works with the Java
  * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
  * uncomment the getString line to get the auto name from the text box below the Gyro
  *

  * <p>You can add additional auto modes by adding additional comparisons to the switch structure
  * below with additional strings. If using the SendableChooser make sure to add them to the
  * chooser code above as well.
  */

@Override
public void autonomousInit() {
  m_autoSelected = m_chooser.getSelected();
  // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
  System.out.println("Auto selected: " + m_autoSelected);
  timer.stop();
}

/** This function is called periodically during autonomous. */
@Override
public void autonomousPeriodic() {
  timer.start();
  switch (m_autoSelected) {
    case kCustomAuto:
      if(timer.get() < 5){
        forward = 0.5;
      }
      if(timer.get() > 5 && timer.get() < 10){
        turn = 0.5;
      }
      break;
    case kDefaultAuto:
    default:
      
      break;
  }

}

/** This function is called once when teleop is enabled. */
@Override
public void teleopInit() {
  comp.enableDigital();
}

/** This function is called periodically during operator control. */
@Override
public void teleopPeriodic() {
  forward = -1 * _xboxDriver.getLeftY();
  turn = _xboxDriver.getRightX();
  forward = (Deadband(forward))*0.7;
  turn = Deadband(turn);

  arm = _xboxOp.getLeftY();

  if (Math.abs(forward) > .1 || Math.abs(turn) > .1) {
    multVelocidad += .05;
  }else{
    multVelocidad-= .05;
  }
  if (multVelocidad > 1) {
    multVelocidad=1;
  }
  if (multVelocidad < .1) {
    multVelocidad=.1;
  }

  if(Math.abs(arm) > .1){
    multArm += .05;
  }else{
    multArm -= .05;
  }
  if(multArm > 1){
    multArm = 1;
  }
  if(multArm < .1){
    multArm = .1;
  }

  if(_xboxDriver.getLeftTriggerAxis() > 0.8){
    /* _Drive_Left_Main.setNeutralMode(NeutralMode.Brake);
    _Drive_Left_Follower.setNeutralMode(NeutralMode.Brake);
    _Drive_Right_Main.setNeutralMode(NeutralMode.Brake);
    _Drive_Right_Follower.setNeutralMode(NeutralMode.Brake);
    _Drive_Left_Main.set(ControlMode.PercentOutput, 0);
    _Drive_Left_Follower.set(ControlMode.PercentOutput, 0);
    _Drive_Right_Main.set(ControlMode.PercentOutput, 0);
    _Drive_Right_Follower.set(ControlMode.PercentOutput, 0); */

    ADrive.stopMotor();
  }else{
    _Drive_Left_Main.setNeutralMode(NeutralMode.Coast);
    _Drive_Left_Follower.setNeutralMode(NeutralMode.Coast);
    _Drive_Right_Main.setNeutralMode(NeutralMode.Coast);
    _Drive_Right_Follower.setNeutralMode(NeutralMode.Coast);
  }

  if(_xboxOp.getAButton()){
    solenoid.toggle();
  }

  if(_xboxOp.getXButtonPressed()){
    comp.disable();
  }else if(_xboxOp.getYButton()){
    comp.enableDigital();
  }
}

/** This function is called once when the robot is disabled. */
@Override
public void disabledInit() {}

/** This function is called periodically when disabled. */
@Override
public void disabledPeriodic() {}

/** This function is called once when test mode is enabled. */
@Override
public void testInit() {}

/** This function is called periodically during test mode. */
@Override
public void testPeriodic() {}

/** This function is called once when the robot is first started up. */
@Override
public void simulationInit() {}

/** This function is called periodically whilst in simulation. */
@Override
public void simulationPeriodic() {}

double Deadband(final double value)
  {
  /* Upper deadband */
  if (value >= 0.05 || value <= -0.05) 
    return value;

  /* Outside deadband */
  return 0;
	}
}