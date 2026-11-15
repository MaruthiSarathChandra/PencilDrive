package com.vanam.pencildrive.events.listener;

import com.vanam.pencildrive.events.InvitationsRequested;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InvitationEventListener {

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )

    public void onInvitationRequested(InvitationsRequested event) {

        //Trigger invitation notification processing
        System.out.println(
                "Processing invitations for group: "
                + event.groupId()
        );
    }

}
