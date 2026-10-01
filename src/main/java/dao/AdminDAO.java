package dao;

import model.Ikan;
import model.Nelayan;
import model.Pembelian;
import model.Pesanan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AdminDAO {
    private final IkanDAO ikanDAO;
    private final NelayanDAO nelayanDAO;
    private final PembelianDAO pembelianDAO;

    public AdminDAO() {
        this.ikanDAO = new IkanDAO();
        this.nelayanDAO = new NelayanDAO();
        this.pembelianDAO = new PembelianDAO();
    }

    public AdminDAO(Connection connection) {
        this.ikanDAO = new IkanDAO(connection);
        this.nelayanDAO = new NelayanDAO(connection);
        this.pembelianDAO = new PembelianDAO(connection);
    }

    // ------------------ CRUD IKAN ------------------
    public List<Ikan> getAllIkan() {
        return ikanDAO.getAllIkan();
    }

    public void addIkan(Ikan ikan) {
        ikanDAO.addIkan(ikan);
    }

    public void updateIkan(Ikan ikan) {
        ikanDAO.updateIkan(ikan);
    }

    public void deleteIkan(int idIkan) {
        ikanDAO.deleteIkan(idIkan);
    }

    // ------------------ CRUD NELAYAN ------------------
    public List<Nelayan> getAllNelayan() {
        return nelayanDAO.getAllNelayan();
    }

    public void addNelayan(Nelayan nelayan) {
        nelayanDAO.addNelayan(nelayan);
    }

    public void updateNelayan(Nelayan nelayan) {
        nelayanDAO.updateNelayan(nelayan);
    }

    public void deleteNelayan(int idNelayan) {
        nelayanDAO.deleteNelayan(idNelayan);
    }

    // ------------------ CRUD PEMBELIAN ------------------
    public List<Pembelian> getAllPembelian() {
        return pembelianDAO.getAllPembelian();
    }

    public void addPembelian(Pembelian pembelian) {
        pembelianDAO.addPembelian(pembelian);
    }

    public void deletePembelian(int idPembelian) {
        pembelianDAO.deletePembelian(idPembelian);
    }
}
