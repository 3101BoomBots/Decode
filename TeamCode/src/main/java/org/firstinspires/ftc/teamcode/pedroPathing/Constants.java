package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(10.6)
            .forwardZeroPowerAcceleration(-30.25621359136083)
            .lateralZeroPowerAcceleration(-57.42076556607728)
            .translationalPIDFCoefficients(new PIDFCoefficients(0.10004, 0.0001, 0, 0))
            .headingPIDFCoefficients(new PIDFCoefficients(1, 0, 0, 0.01))
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.03, 0.03,0.000001 , 0.6, 0.1))
            .centripetalScaling(0.00049);

    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .rightFrontMotorName("fr")
            .rightRearMotorName("br")
            .leftFrontMotorName("fl")
            .leftRearMotorName("bl")
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .xVelocity(55.81044498954234)
            .yVelocity(36.0155913585753);

    public static PathConstraints pathConstraints = new PathConstraints(0.99, 100, 0.5, 1);
    public static PinpointConstants pinpointConstants = new PinpointConstants()
            .hardwareMapName("pinpoint")
            .distanceUnit(DistanceUnit.INCH)
            .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD)
            .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
            .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
            .strafePodX(-6.575)
            .forwardPodY(4.0551);
    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pinpointLocalizer(pinpointConstants)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .build();
    }
}
