package com.bamc.corebanking.domain.events;

import com.bamc.corebanking.domain.Amount;

public record TransferRejected(Amount amount) {

}
