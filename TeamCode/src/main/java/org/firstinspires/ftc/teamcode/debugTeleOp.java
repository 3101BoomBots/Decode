package org.firstinspires.ftc.teamcode;


import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="debugTeleOp")
public class debugTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);
        boolean usingVelocity = true;
        int curr = 0;
        int pos = 0;
        hw.init(hardwareMap);
        TelemetryManager panels = PanelsTelemetry.INSTANCE.getTelemetry();
        waitForStart();
//        hw.indexerDown(1);
//        while(!hw.outtakeMotorLeft.isBusy());
        while(opModeIsActive()) {
            if (gamepad1.bWasPressed()) {
                hw.intakeMotor.setPower(1);
            }
            if (gamepad1.aWasPressed()) {
                if(!hw.indexerMotor.getMode().equals(DcMotor.RunMode.RUN_TO_POSITION)) hw.indexerRun();
                usingVelocity = false;
                hw.indexerUp(1);
            }
            if (gamepad1.xWasPressed()) {
                if(!hw.indexerMotor.getMode().equals(DcMotor.RunMode.RUN_TO_POSITION)) hw.indexerRun();
                usingVelocity = false;
                hw.indexerDown(1);
            }
            if (gamepad1.dpadRightWasPressed()) {
                if(!hw.indexerMotor.getMode().equals(DcMotor.RunMode.RUN_USING_ENCODER)) hw.indexerVelocity();
                pos = hw.velocityIndexerUp();
                usingVelocity = true;
            }
            else if (gamepad1.dpadLeftWasPressed()) {
                if(!hw.indexerMotor.getMode().equals(DcMotor.RunMode.RUN_USING_ENCODER)) hw.indexerVelocity();
                pos = hw.velocityIndexerDown();
                usingVelocity = true;
            }
//            if () {
//                hw.indexerMotor.setVelocity(-120);
//            }
            if (Math.abs(pos - hw.indexerMotor.getCurrentPosition()) <= 13 && usingVelocity) {
                hw.indexerMotor.setPower(0);
                hw.indexerMotor.setVelocity(0);
            }
            panels.addData("velocity", Math.abs(hw.indexerMotor.getVelocity()));
//            panels.addData("current", hw.indexerMotor.getCurrentPosition());
//            panels.addData("target", pos);
            panels.addData("error", Math.abs(pos-hw.indexerMotor.getCurrentPosition()));
            panels.update(telemetry);
        }
    }
}
