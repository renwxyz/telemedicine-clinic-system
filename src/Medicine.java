package domain;

public class Medicine implements Comparable<Medicine> {
    // Encapsulation ketat: atribut private
    private final String medicineId;
    private String name;
    private String description;
    private double price;
    private int stock;
    private boolean requiresPrescription;

    // Constructor
    public Medicine(String medicineId, String name, String description, double price, int stock, boolean requiresPrescription) {
        this.medicineId = medicineId;
        this.name = name;
        this.description = description;
        
        // Memanggil setter untuk memicu validasi saat objek dibuat
        setPrice(price); 
        setStock(stock); 
        this.requiresPrescription = requiresPrescription;
    }

    // --- GETTER ---
    public String getMedicineId() { return medicineId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isRequiresPrescription() { return requiresPrescription; }

    // --- SETTER & VALIDASI ---
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Harga obat tidak boleh negatif.");
        }
        this.price = price;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stok awal tidak boleh kurang dari 0.");
        }
        this.stock = stock;
    }

    // --- BUSINESS LOGIC ---
    public boolean isInStock(int qty) {
        return this.stock >= qty;
    }

    public void reduceStock(int qty) throws OutOfStockException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Jumlah pengurangan stok harus lebih dari 0.");
        }
        if (!isInStock(qty)) {
            throw new OutOfStockException("Stok tidak mencukupi untuk obat: " + this.name + ". Sisa stok: " + this.stock);
        }
        this.stock -= qty;
    }

    // --- IMPLEMENTASI COMPARABLE ---
    @Override
    public int compareTo(Medicine other) {
        // Mengurutkan berdasarkan nama secara alfabetis (A-Z)
        return this.name.compareToIgnoreCase(other.name);
    }
}