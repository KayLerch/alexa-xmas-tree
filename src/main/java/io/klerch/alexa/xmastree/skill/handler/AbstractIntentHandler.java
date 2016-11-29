package io.klerch.alexa.xmastree.skill.handler;

import io.klerch.alexa.state.handler.AWSIotStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.AlexaIntentHandler;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.xmastree.skill.model.TreeState;

abstract class AbstractIntentHandler implements AlexaIntentHandler {
    @Override
    public boolean verify(final AlexaInput input) {
        return true;
    }

    @Override
    public abstract AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException;

    @Override
    public AlexaOutput handleError(final AlexaRequestHandlerException exception) {
        return null;
    }

    static void sendIotHook(final AlexaInput input, final TreeState treeState) throws AlexaStateException {
        final AWSIotStateHandler iotStateHandler = new AWSIotStateHandler(input.getSessionStateHandler().getSession());
        treeState.withHandler(iotStateHandler).saveState();
    }
}
