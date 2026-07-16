package moh.gov.zm.lis.print.util;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.config.PrinterProperties;
import moh.gov.zm.lis.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Transcodes a lab-order id into ZPL II for the Zebra ZD421: a Code 128 barcode
 * with a human-readable interpretation line, laid out across N label strips (one
 * ZPL label format per strip, each stamped "n of total" so the physical strips
 * can be told apart). The ZD421 renders the barcode itself from this ZPL.
 */
@Component
@RequiredArgsConstructor
public class ZplLabelEncoder {
    /** Order ids safe to embed in a ^FD field without ZPL escaping. */
    private static final Pattern SAFE_ORDER_ID = Pattern.compile("[A-Za-z0-9._\\-]{1,64}");
    private static final int MARGIN_X = 20;

    private final PrinterProperties props;

    /** Build the ZPL for {@code strips} identical barcode labels of {@code orderId}. */
    public String encode(String orderId, int strips) {
        validate(orderId);
        int total = Math.max(1, strips);
        StringBuilder zpl = new StringBuilder(total * 160);
        for (int i = 1; i <= total; i++) {
            zpl.append(label(orderId, i, total));
        }
        return zpl.toString();
    }

    private String label(String orderId, int index, int total) {
        return "^XA\n"
                + "^CI28\n"                                        // UTF-8
                + "^PW" + props.getPrintWidthDots() + "\n"          // print width
                + "^LL" + props.getLabelLengthDots() + "\n"         // label length
                + "^LH0,0\n"                                        // label home (origin)
                + "^FO" + MARGIN_X + ",10^A0N,20,20^FD" + index + " of " + total + "^FS\n"
                + "^FO" + MARGIN_X + ",34"
                + "^BY" + props.getBarcodeModuleWidth()
                + "^BCN," + props.getBarcodeHeightDots() + ",Y,N,N"  // Code128, print human-readable line, no check digit
                + "^FD" + orderId + "^FS\n"
                + "^XZ\n";
    }

    private void validate(String orderId) {
        if (orderId == null || !SAFE_ORDER_ID.matcher(orderId).matches()) {
            throw new ValidationException("Order id cannot be encoded to a Code 128 barcode",
                    Map.of("orderId", "must be 1–64 characters of letters, digits, '.', '_' or '-'"));
        }
    }
}
