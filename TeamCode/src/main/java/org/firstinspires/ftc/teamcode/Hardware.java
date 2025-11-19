package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Hardware {
    private static Hardware self;
    public final int OPEN_POSITION_FLAP = 54;
    public final int CLOSED_POSITION_FLAP = 20;
    public final int INDEXER_RESOLUTION = 538;
    private OpMode opMode;
    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;
    public DcMotorEx indexerMotor;
    public DcMotorEx outtakeMotorLeft;
    public DcMotorEx outtakeMotorRight;
    public DcMotorEx intakeMotor;
    public Servo nathanMikhailServo;

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

        frontLeft = hardwareMap.get(DcMotor.class, "fl");
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);

        frontRight = hardwareMap.get(DcMotor.class, "fr");
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        backRight = hardwareMap.get(DcMotor.class, "br");
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        backLeft = hardwareMap.get(DcMotor.class, "bl");
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        nathanMikhailServo = hardwareMap.get(Servo.class, "flapServo");
        nathanMikhailServo.setPosition(CLOSED_POSITION_FLAP);

        indexerMotor = hardwareMap.get(DcMotor.class, "indexerMotor");
        indexerMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        indexerMotor.setTargetPosition(0);
        indexerMotor.setPower(0.8);
        indexerMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        outtakeMotorRight = hardwareMap.get(DcMotor.class, "outtakeMotorRight");
        outtakeMotorRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        outtakeMotorLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        outtakeMotorLeft = hardwareMap.get(DcMotor.class, "outtakeMotorLeft");
        outtakeMotorLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void setToRunToPosition() {
        setPower(0);
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
}
