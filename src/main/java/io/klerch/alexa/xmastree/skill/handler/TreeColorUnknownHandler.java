package io.klerch.alexa.xmastree.skill.handler;

import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;

@AlexaIntentListener(customIntents = "TreeColor", priority = 1)
public class TreeColorUnknownHandler extends AbstractIntentHandler {
    @Override
    public AlexaOutput handleRequest(AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        return AlexaOutput.tell("SayColorUnknown").build();
    }
}
