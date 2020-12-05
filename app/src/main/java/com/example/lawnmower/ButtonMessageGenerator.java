package com.example.lawnmower;

public class ButtonMessageGenerator {

    public static AppControlsProtos.AppControls buildMessage(int i) {
        AppControlsProtos.AppControls btn;
        switch(i) {
            case (1):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.START).build();
                break;
            case(2):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.STOP).build();
                break;
            case(3):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.PAUSE).build();
                break;
            case(4):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.HOME).build();
                break;
            default:
                btn = null;
                break;
        }
        return btn;
    }
}