package com.damalert.ddas.dam.application;

import java.util.UUID;

public interface DamReader {

	DamSummary requireDam(UUID damId);
}
