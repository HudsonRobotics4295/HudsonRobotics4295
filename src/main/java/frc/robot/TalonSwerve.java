package frc.robot;

import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.kinematics.SwerveModuleState;

//import edu.wpi.first.wpilibj.Encoder;

public class TalonSwerve {
    //private static final double kWheelRadius = 0.0508;
    //private static final int kEncoderResolution = 4096;

    private static final double kModuleMaxAngularVelocity = Drivetrain.kMaxAngularSpeed;
    private static final double kModuleMaxAngularAcceleration = 2 * Math.PI; // radians per second squared

    private TalonSRX turningMotor;
    private TalonFX driveMotor;

    // private Encoder m_driveEncoder;
    // private Encoder m_turningEncoder;

    // Gains are for example purposes only - must be determined for your own robot!
    private final PIDController m_drivePIDController = new PIDController(1, 0, 0);

    // Gains are for example purposes only - must be determined for your own robot!
    private final ProfiledPIDController m_turningPIDController = new ProfiledPIDController(
            1,
            0,
            0,
            new TrapezoidProfile.Constraints(
                    kModuleMaxAngularVelocity, kModuleMaxAngularAcceleration));

    // Gains are for example purposes only - must be determined for your own robot!
    private final SimpleMotorFeedforward m_driveFeedforward = new SimpleMotorFeedforward(1, 3);
    private final SimpleMotorFeedforward m_turnFeedforward = new SimpleMotorFeedforward(1, 0.5);

    public TalonSwerve(int driveMotorCanbusAddress, int turningMotorCanbusAddress) {
        turningMotor = new TalonSRX(turningMotorCanbusAddress);
        driveMotor = new TalonFX(driveMotorCanbusAddress);

        // Set the distance per pulse for the drive encoder. We can simply use the
        // distance traveled for one rotation of the wheel divided by the encoder
        // resolution.
        //m_driveEncoder.setDistancePerPulse(2 * Math.PI * kWheelRadius / kEncoderResolution);

        // Set the distance (in this case, angle) per pulse for the turning encoder.
        // This is the the angle through an entire rotation (2 * pi) divided by the
        // encoder resolution.
        //m_turningEncoder.setDistancePerPulse(2 * Math.PI / kEncoderResolution);

        // Limit the PID Controller's input range between -pi and pi and set the input
        // to be continuous.
        m_turningPIDController.enableContinuousInput(-Math.PI, Math.PI);
    }

    public SwerveModuleState getState() {
        // return new SwerveModuleState(m_driveEncoder.getRate(), new
        // Rotation2d(m_turningEncoder.get()));
        return new SwerveModuleState(driveMotor.getSelectedSensorVelocity(),
                new Rotation2d(turningMotor.getSelectedSensorPosition()));
    }

    public void setDesiredState(SwerveModuleState desiredState) {
        // Optimize the reference state to avoid spinning further than 90 degrees
        //SwerveModuleState state = SwerveModuleState.optimize(desiredState, new Rotation2d(m_turningEncoder.get()));
        SwerveModuleState state = SwerveModuleState.optimize(desiredState, new Rotation2d(turningMotor.getSelectedSensorPosition()));

        // Calculate the drive output from the drive PID controller.
        // final double driveOutput =
        // m_drivePIDController.calculate(m_driveEncoder.getRate(),
        // state.speedMetersPerSecond);
        final double driveOutput = m_drivePIDController.calculate(driveMotor.getSelectedSensorPosition(),
                state.speedMetersPerSecond);

        final double driveFeedforward = m_driveFeedforward.calculate(state.speedMetersPerSecond);

        // Calculate the turning motor output from the turning PID controller.
        // final double turnOutput =
        // m_turningPIDController.calculate(m_turningEncoder.get(),
        // state.angle.getRadians());
        final double turnOutput = m_turningPIDController.calculate(turningMotor.getSelectedSensorPosition(),
                state.angle.getRadians());

        final double turnFeedforward = m_turnFeedforward.calculate(m_turningPIDController.getSetpoint().velocity);

        // m_driveMotor.setVoltage(driveOutput + driveFeedforward);
        // m_turningMotor.setVoltage(turnOutput + turnFeedforward);
        driveMotor.set(TalonFXControlMode.PercentOutput, driveOutput + driveFeedforward);
        turningMotor.set(TalonSRXControlMode.PercentOutput, turnOutput + turnFeedforward);
    }
}