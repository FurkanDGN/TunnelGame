package com.github.furkandgn.tunnelgame.common.config.pojo;

import com.gmail.furkanaxx34.dlibrary.xseries.XMaterial;
import com.github.furkandgn.tunnelgame.common.upgrade.Applicable;

/**
 * @author Furkan Doğan
 */
public record UpgradeConfig(int requiredPoints, XMaterial icon, Applicable applicable) {
}
