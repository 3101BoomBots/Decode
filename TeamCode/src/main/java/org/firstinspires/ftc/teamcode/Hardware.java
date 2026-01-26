package org.firstinspires.ftc.teamcode;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@Configurable
public class Hardware {
    private static Hardware self;
    public final static double INDEXER_RESOLUTION = 537.7;
    private OpMode opMode;
    public GoBildaPinpointDriver pinpoint;
    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;
    public DcMotorEx indexerMotor;
    public DcMotorEx outtakeMotorLeft;
    public DcMotorEx outtakeMotorRight;
    public DcMotorEx intakeMotor;
    public static int indexerVelocity = 1500;
    public RevColorSensorV3 color1;
    public RevColorSensorV3 color2;
    public Limelight3A limelight;

    private PIDFCoefficients pidfCoefficients = new PIDFCoefficients(80, 0, 0, 14.5);


    private Hardware(OpMode opMode) {
        self = this;
        this.opMode = opMode;
    }

    public static Hardware getInstance(OpMode opMode) {
        if (self == null) self = new Hardware(opMode);
        return self;
    }

    // intake - in/out/stop
    //outtake - stop/out
    //flap up down
    // indexer - move
    //trigger if > 0.1
    //button gamepad 1
    //true false bumpers

    public void init(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotorEx.class, "fl");
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        frontRight = hardwareMap.get(DcMotorEx.class, "fr");
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);

        backRight = hardwareMap.get(DcMotorEx.class, "br");
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        backLeft = hardwareMap.get(DcMotorEx.class, "bl");
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        pedroInit(hardwareMap);

//        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
//        pinpoint.resetPosAndIMU();
//        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD, GoBildaPinpointDriver.EncoderDirection.FORWARD);
//        pinpoint.setOffsets(0, 0, DistanceUnit.INCH);
//        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
//        pinpoint.setPosition(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0));
    }

    public void indexerRun() {
        indexerMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        indexerMotor.setTargetPosition(0);
        indexerMotor.setPower(1);
        indexerMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        indexerMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    public void indexerVelocity() {
        indexerMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        indexerMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void pedroInit(HardwareMap hardwareMap) {
        indexerMotor = hardwareMap.get(DcMotorEx.class, "indexerMotor");
        indexerMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        indexerMotor.setTargetPosition(0);
//        indexerMotor.setPower(1);
//        indexerMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        indexerMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        indexerVelocity();

        intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        outtakeMotorRight = hardwareMap.get(DcMotorEx.class, "outtakeMotorRight");
        outtakeMotorRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        outtakeMotorRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        outtakeMotorRight.setDirection(DcMotorSimple.Direction.REVERSE);

        outtakeMotorLeft = hardwareMap.get(DcMotorEx.class, "outtakeMotorLeft");
        outtakeMotorLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        outtakeMotorLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        outtakeMotorRight.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);
        outtakeMotorLeft.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);

        color1 = hardwareMap.get(RevColorSensorV3.class, "color1");
        color2 = hardwareMap.get(RevColorSensorV3.class, "color2");
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
    }

    public void setToRunToPosition() {
        setPower(0);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setTarget(0);
        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    public void setPower(double power) {
        frontLeft.setPower(power);
        frontRight.setPower(power);
        backLeft.setPower(power);
        backRight.setPower(power);
    }

    public void setTarget(int ticks) {
        frontLeft.setTargetPosition(ticks);
        frontRight.setTargetPosition(ticks);
        backLeft.setTargetPosition(ticks);
        backRight.setTargetPosition(ticks);
    }

    public void setToNoEncoder() {
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public void setWithEncoder() {
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void strafeTicks(int ticks) { // pos = right
        frontLeft.setTargetPosition(ticks);
        frontRight.setTargetPosition(-ticks);
        backRight.setTargetPosition(ticks);
        backLeft.setTargetPosition(-ticks);
    }


    public void strafePower(double power) { // pos = right
        frontLeft.setPower(power);
        frontRight.setPower(-power);
        backRight.setPower(power);
        backLeft.setPower(-power);
    }

    public void indexerDown(int turns) {
        indexerMotor.setTargetPosition(indexerMotor.getTargetPosition() - (int)(turns*0.333*INDEXER_RESOLUTION));
    }

    public void indexerUp(int turns) {
        indexerMotor.setTargetPosition(indexerMotor.getTargetPosition() + (int)(turns*0.333*INDEXER_RESOLUTION));
    }

    public int velocityIndexerDown() {
        int curr = indexerMotor.getCurrentPosition();
        indexerMotor.setVelocity(-indexerVelocity);
        return (int) (curr - 0.333 * INDEXER_RESOLUTION);
    }

    public int velocityIndexerUp() {
        int curr = indexerMotor.getCurrentPosition();
        indexerMotor.setVelocity(indexerVelocity);
        return (int) (curr + 0.333 * INDEXER_RESOLUTION);
    }

    public void outtake(int velocity) {
        outtakeMotorLeft.setVelocity(velocity);
        outtakeMotorRight.setVelocity(velocity);
    }
}
