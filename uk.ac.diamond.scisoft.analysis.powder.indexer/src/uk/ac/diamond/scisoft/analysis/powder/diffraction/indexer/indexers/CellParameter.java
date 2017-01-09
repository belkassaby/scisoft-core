package uk.ac.diamond.scisoft.analysis.powder.diffraction.indexer.indexers;

import uk.ac.diamond.scisoft.xpdf.views.CrystalSystem;

import uk.ac.diamond.scisoft.xpdf.views.XPDFPhase;
import uk.ac.diamond.scisoft.xpdf.views.XPDFPhaseForm;

/**
 *
 *         Structure of cell parameters receive after indexing.
 *
 *         A wrapper over XPDFPhase to add specfic factors found with cells.
 *         
 * @author Dean P. Ottewell
 */
public class CellParameter extends XPDFPhase {

	private Double merit;

	public CellParameter() {
		setForm(XPDFPhaseForm.get(XPDFPhaseForm.Forms.CRYSTALLINE)); 

		CrystalSystem system = new CrystalSystem();
		// Extract crystal system indexing found
		setCrystalSystem(system); // Shouldnt really be having to set this
		// setCrystalSystem(inSystem);

		// setUnitCellAngles(a, b, c);
		// setUnitCellLengths(a, b, c);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		CellParameter cell = (CellParameter) obj;

		return (cell.getUnitA().equals(this.getUnitA()) || cell.getUnitB().equals(this.getUnitB())
				|| cell.getUnitC().equals(this.getUnitC()) || cell.getUnitCellAngle().equals(this.getUnitCellAngle()));

	}

	public void setFigureMerit(double m) {
		merit = m;
	}

	public Double getFigureMerit() {
		return merit;
	}

	public Double getUnitA() {
		return getUnitCellLength(0);
	}

	public Double getUnitB() {
		return getUnitCellLength(1);
	}

	public Double getUnitC() {
		return getUnitCellLength(2);
	}

	public Double getAngleAlpha() {
		double[] angs = getUnitCellAngle();
		return angs[0];

	}

	public Double getAngleBeta() {
		double[] angs = getUnitCellAngle();
		return angs[1];
	}

	public Double getAngleGamma() {
		double[] angs = getUnitCellAngle();
		return angs[2];
	}

}