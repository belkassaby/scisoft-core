package org.dawnsci.surfacescatter;

import java.util.ArrayList;

import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;

public class GoodPointStripper {

	public IDataset[][] goodPointStripper(CurveStitchDataPackage csdp, AxisEnums.yAxes ids, AxisEnums.xAxes xds) {

		IDataset[] goodPointIDatasets = csdp.getGoodPointIDataset();

		IDataset[] xIDatasets = csdp.getxIDataset();

		switch (xds) {
		case SCANNED_VARIABLE:
			xIDatasets = csdp.getxIDataset();
			break;
		case Q:
			xIDatasets = csdp.getqIDataset();
			break;
		default:
		}

		int noOfDats = xIDatasets.length;

		IDataset[] yIDatasets = csdp.getyIDataset();
		IDataset[] yIDatasetsErrors = csdp.getyIDatasetError();

		if (ids == null) {
			ids = yAxes.SPLICEDY;
		}

		switch (ids) {
		case SPLICEDY:
			yIDatasets = csdp.getyIDataset();
			yIDatasetsErrors = csdp.getyIDatasetError();
			break;

		case SPLICEDYRAW:
			yIDatasets = csdp.getyRawIDataset();
			yIDatasetsErrors = csdp.getyRawIDatasetError();
			break;

		case SPLICEDYFHKL:
			yIDatasets = csdp.getyIDatasetFhkl();
			yIDatasetsErrors = csdp.getyIDatasetFhklError();
			break;

		default:

		}

		IDataset[][] output = new IDataset[noOfDats][];

		for (int i = 0; i < noOfDats; i++) {

			ArrayList<Double> xHolder = new ArrayList<>();
			ArrayList<Double> yHolder = new ArrayList<>();
			ArrayList<Double> yErrorHolder = new ArrayList<>();

			for (int j = 0; j < goodPointIDatasets[i].getSize(); j++) {
				if (goodPointIDatasets[i].getBoolean(j)) {

					xHolder.add(xIDatasets[i].getDouble(j));
					yHolder.add(yIDatasets[i].getDouble(j));
					yErrorHolder.add(yIDatasetsErrors[i].getDouble(j));
				}
			}
			if (!xHolder.isEmpty()) {
				IDataset xH = DatasetFactory.createFromList(xHolder);
				IDataset yH = DatasetFactory.createFromList(yHolder);
				IDataset yEH = DatasetFactory.createFromList(yErrorHolder);

				IDataset[] chunk = new IDataset[] { xH, yH, yEH };

				output[i] = chunk;
			}
		}

		return output;
	}

	public IDataset[] splicedYGoodPointStripper(CurveStitchDataPackage csdp, AxisEnums.yAxes ids, boolean includeAll) {

		IDataset goodPointIDatasets = DatasetFactory.createFromObject(true);

		IDataset yIDatasets = csdp.getSplicedCurveY();
		IDataset yIDatasetsErrors = csdp.getSplicedCurveYError();

		if (csdp.getSplicedGoodPointIDataset() != null) {
			goodPointIDatasets = csdp.getSplicedGoodPointIDataset();
		}

		else {
			ArrayList<Boolean> bh = new ArrayList<>();

			for (int u = 0; u < yIDatasets.getSize(); u++) {
				bh.add(true);
			}

			goodPointIDatasets = DatasetFactory.createFromList(bh);
		}

		switch (ids) {
		case SPLICEDY:
			break;

		case SPLICEDYRAW:
			yIDatasets = csdp.getSplicedCurveYRaw();
			yIDatasetsErrors = csdp.getSplicedCurveYRawError();
			break;

		case SPLICEDYFHKL:
			yIDatasets = csdp.getSplicedCurveYFhkl();
			yIDatasetsErrors = csdp.getSplicedCurveYFhklError();
			break;

		default:
		}

		ArrayList<Double> yHolder = new ArrayList<>();
		ArrayList<Double> yErrorHolder = new ArrayList<>();

		for (int j = 0; j < yIDatasets.getSize(); j++) {

			if (includeAll) {

				yHolder.add(yIDatasets.getDouble(j));
				yErrorHolder.add(yIDatasetsErrors.getDouble(j));

			}

			else {
				try {
					if (goodPointIDatasets.getBoolean(j)) {

						yHolder.add(yIDatasets.getDouble(j));
						yErrorHolder.add(yIDatasetsErrors.getDouble(j));
					}
				} catch (IndexOutOfBoundsException d) {

				}
			}
		}

		IDataset yH = DatasetFactory.createFromList(yHolder);
		IDataset yEH = DatasetFactory.createFromList(yErrorHolder);

		return new IDataset[] { yH, yEH };
	}

	public IDataset splicedXGoodPointStripper(CurveStitchDataPackage csdp, AxisEnums.xAxes ids, boolean includeAll) {

		IDataset goodPointIDatasets = DatasetFactory.createFromObject(true);

		IDataset xIDataset = csdp.getSplicedCurveX();

		if (csdp.getSplicedGoodPointIDataset() != null) {
			goodPointIDatasets = csdp.getSplicedGoodPointIDataset();
		}

		else {
			ArrayList<Boolean> bh = new ArrayList<>();

			for (int u = 0; u < xIDataset.getSize(); u++) {
				bh.add(true);
			}

			goodPointIDatasets = DatasetFactory.createFromList(bh);
		}

		switch (ids) {
		case SCANNED_VARIABLE:
			break;

		case Q:
			xIDataset = csdp.getSplicedCurveQ();
			break;

		default:

		}

		ArrayList<Double> xHolder = new ArrayList<>();

		for (int j = 0; j < xIDataset.getSize(); j++) {
			if (includeAll) {
				xHolder.add(xIDataset.getDouble(j));
			} else {
				try {
					if (goodPointIDatasets.getBoolean(j)) {

						xHolder.add(xIDataset.getDouble(j));
					}
				} catch (IndexOutOfBoundsException d) {

				}
			}
		}

		return DatasetFactory.createFromList(xHolder);
	}

	private IDataset getYIDataset(CurveStitchDataPackage csdp, AxisEnums.yAxes y) {

		switch (y) {
		case SPLICEDY:
			return csdp.getSplicedCurveY();
		case SPLICEDYRAW:
			return csdp.getSplicedCurveYRaw();
		case SPLICEDYFHKL:
			return csdp.getSplicedCurveYFhkl();
		default:
			//
		}

		return csdp.getSplicedCurveY();
	}

	private IDataset getYeIDataset(CurveStitchDataPackage csdp, AxisEnums.yAxes y) {

		switch (y) {
		case SPLICEDY:
			return csdp.getSplicedCurveYError();
		case SPLICEDYRAW:
			return csdp.getSplicedCurveYRawError();
		case SPLICEDYFHKL:
			return csdp.getSplicedCurveYFhklError();
		default:
			//
		}

		return csdp.getSplicedCurveYError();
	}

	private IDataset getXIDataset(CurveStitchDataPackage csdp, AxisEnums.xAxes x) {

		switch (x) {
		case SCANNED_VARIABLE:
			return csdp.getSplicedCurveX();
		case Q:
			return csdp.getSplicedCurveQ();
		default:
			//
		}

		return csdp.getSplicedCurveX();
	}

}
