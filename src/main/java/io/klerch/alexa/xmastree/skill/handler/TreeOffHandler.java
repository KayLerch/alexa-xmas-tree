package io.klerch.alexa.xmastree.skill.handler;

import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.xmastree.skill.model.TreeState;

@AlexaIntentListener(customIntents = "TreeOff")
public class TreeOffHandler extends AbstractIntentHandler {
    @Override
    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final AWSDynamoStateHandler dynamoHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());
        final TreeState treeState = dynamoHandler.readModel(TreeState.class).orElse(new TreeState(input.getLocale(), TreeState.MODE.ON));

        // set state off
        treeState.setState(TreeState.MODE.OFF);

        // send state of tree to AWS IoT thing shadow
        sendIotHook(input, treeState);

        return AlexaOutput.tell("SayTreeOff")
                .putState(treeState.withHandler(dynamoHandler))
                .build();
    }
}
