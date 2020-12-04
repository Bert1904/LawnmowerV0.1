package com.example.lawnmower;

public class JoystickMessageGenerator {


    public AppControlsProtos.AppControls buildMessage(double X, double Y) {
        AppControlsProtos.AppControls.JoyStick joystick = AppControlsProtos.AppControls.JoyStick.newBuilder().setX(X).setY(Y).build();
        return AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.NO_CMD).setJoy(joystick).build();
    }
}

