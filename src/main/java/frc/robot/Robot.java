// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.TimedRobot;
/* import edu.wpi.first.wpilibj.Timer; */
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
  Boolean up = false;
  Boolean down = false;

  DifferentialDrive ADrive = new DifferentialDrive(_Drive_Left_Main, _Drive_Right_Main);

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

    _Drive_Left_Main.setInverted(false);
    _Drive_Left_Follower.setInverted(false);
    _Drive_Right_Main.setInverted(false);
    _Drive_Right_Follower.setInverted(false);
    _Arm.setInverted(false);

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
    if(up){
      _Arm.setNeutralMode(NeutralMode.Coast);
      _Arm.set(ControlMode.PercentOutput, 0.5);
    }else if(down){
      _Arm.setNeutralMode(NeutralMode.Coast);
      _Arm.set(ControlMode.PercentOutput, -0.5);
    }else{
      _Arm.setNeutralMode(NeutralMode.Brake);
    }

    ADrive.arcadeDrive(-forward, -turn);
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
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    forward = -1 * _xboxDriver.getLeftY();
    turn = _xboxDriver.getRightX();
    forward = Deadband(forward);
    turn = Deadband(turn);

    if(_xboxDriver.getLeftTriggerAxis() > 0.8){
      _Drive_Left_Main.setNeutralMode(NeutralMode.Brake);
      _Drive_Left_Follower.setNeutralMode(NeutralMode.Brake);
      _Drive_Right_Main.setNeutralMode(NeutralMode.Brake);
      _Drive_Right_Follower.setNeutralMode(NeutralMode.Brake);
    }else{
      _Drive_Left_Main.setNeutralMode(NeutralMode.Coast);
      _Drive_Left_Follower.setNeutralMode(NeutralMode.Coast);
      _Drive_Right_Main.setNeutralMode(NeutralMode.Coast);
      _Drive_Right_Follower.setNeutralMode(NeutralMode.Coast);
    }

    if(_xboxOp.getRightBumper()){
      up = true;
      down = false;
    }else if(_xboxOp.getLeftBumper()){
      up = false;
      down = true;
    }else{
      up = false;
      down = false;
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
		if (value >= 0.01 || value <= -0.01) 
			return value;

		/* Outside deadband */
		return 0;
	}
}
