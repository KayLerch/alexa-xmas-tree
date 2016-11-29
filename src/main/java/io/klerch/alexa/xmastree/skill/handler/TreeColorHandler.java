package io.klerch.alexa.xmastree.skill.handler;

import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.model.AlexaOutputSlot;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.schema.type.AlexaOutputFormat;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.xmastree.skill.SkillConfig;
import io.klerch.alexa.xmastree.skill.model.TreeState;

@AlexaIntentListener(customIntents = "TreeColor", priority = 100)
public class TreeColorHandler extends AbstractIntentHandler {
    private TreeState treeState;

    @Override
    public boolean verify(AlexaInput input) {
        treeState = new TreeState(input.getLocale(), input.getSlotValue("Color"));
        return treeState.getColor() != null;
    }

    @Override
    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        // send iot hook
        final AWSDynamoStateHandler dynamoHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());

        treeState.setState(TreeState.MODE.COLOR);

        // send state of tree to AWS IoT thing shadow
        sendIotHook(input, treeState);

        return AlexaOutput.tell("SayColorChange")
                .putState(treeState.withHandler(dynamoHandler))
                .putSlot(new AlexaOutputSlot("mp3", SkillConfig.getMp3IntroUrl()).formatAs(AlexaOutputFormat.AUDIO))
                .build();
    }
}
