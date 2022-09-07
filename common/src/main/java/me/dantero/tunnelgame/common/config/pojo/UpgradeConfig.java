package me.dantero.tunnelgame.common.config.pojo;

import com.gmail.furkanaxx34.dlibrary.xseries.XMaterial;
import me.dantero.tunnelgame.common.upgrade.Applicable;

/**
 * @author Furkan Doğan
 */
public record UpgradeConfig(int requiredPoints, XMaterial icon, Applicable applicable) {
}
