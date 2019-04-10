package bacnet.scripts.core.stat;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.scripts.core.stat.StatTest.TypeStat;

public class FDR {

	public static void adjust(ExpressionMatrix compMatrix, StatTest test, boolean tiling) {
		int m = compMatrix.getNumberRow();
		for (int j = 0; j < compMatrix.getNumberColumn(); j++) {
			String header = compMatrix.getHeader(j);
			// search for the column containing FDRBH, FDRBY, or FDRBONF
			if (header.contains(test.getType().name())) {

				if (test.getType() == TypeStat.FDRBONF) {
					for (String rowName : compMatrix.getOrderedRowNames()) {
						double value = compMatrix.getValue(rowName, header);
						double adjustedValue = m * value;
						if (adjustedValue > 1)
							adjustedValue = 1;
						compMatrix.setValue(adjustedValue, rowName, header);
					}
				} else if (test.getType() == TypeStat.FDRBH) {
					// organize column by increasing order
					compMatrix.sort(j);
					int i = 0;
					for (String rowName : compMatrix.getOrderedRowNames()) {
						double value = compMatrix.getValue(rowName, header);
						double adjustedValue = ((double) m / (double) (i + 1)) * value; // i+1 and not i because i begin
																						// at 0 and not 1
						// we need to conserve the order, so we compare to the next value
						if (i < compMatrix.getOrderedRowNames().size() - 1) {
							double nextValue = compMatrix.getValue(compMatrix.getOrderedRowNames().get(i + 1), header);
							double nextAdjustedValue = ((double) m / (double) (i + 2)) * nextValue;
							if (nextAdjustedValue < adjustedValue)
								adjustedValue = nextAdjustedValue;
						}
						if (adjustedValue > 1)
							adjustedValue = 1;
						compMatrix.setValue(adjustedValue, rowName, header);
						i++;
					}
				} else if (test.getType() == TypeStat.FDRBY) {
					// organize column by increasing order
					compMatrix.sort(j);
					int i = 0;
					double sum = 0;
					for (int k = 1; k <= m; k++) {
						sum += (double) 1 / (double) k;
					}

					for (String rowName : compMatrix.getOrderedRowNames()) {
						double value = compMatrix.getValue(rowName, header);
						double adjustedValue = sum * ((double) m / (double) (i + 1)) * value; // i+1 and not i because i
																								// begin at 0 and not 1
						// we need to conserve the order, so we compare to the next value
						if (i < compMatrix.getOrderedRowNames().size() - 1) {
							double nextValue = compMatrix.getValue(compMatrix.getOrderedRowNames().get(i + 1), header);
							double nextAdjustedValue = sum * ((double) m / (double) (i + 2)) * nextValue;
							if (nextAdjustedValue < adjustedValue)
								adjustedValue = nextAdjustedValue;
						}
						if (adjustedValue > 1)
							adjustedValue = 1;
						compMatrix.setValue(adjustedValue, rowName, header);
						i++;
					}
				}

			}
		}

	}
}
