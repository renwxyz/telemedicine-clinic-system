package domain;

public class PrescriptionItem {
    // Atribut sesuai rancangan PDF
    private Medicine medicine;
    private int quantity;
    private String dosage;
    private String instructions;

    // Constructor
    public PrescriptionItem(Medicine medicine, int quantity, String dosage, String instructions) {
        this.medicine = medicine;
        this.quantity = quantity;
        this.dosage = dosage;
        this.instructions = instructions;
    }

    // --- GETTER ---
    public Medicine getMedicine() { return medicine; }
    public int getQuantity() { return quantity; }
    public String getDosage() { return dosage; }
    public String getInstructions() { return instructions; }
}