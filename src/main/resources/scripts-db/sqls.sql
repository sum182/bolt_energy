SELECT * FROM bolt_energy_db.ralie_usina_csv_import;
SELECT count(*) FROM bolt_energy_db.ralie_usina_csv_import;
select cod_ceg, nom_Empreendimento, mda_potencia_outorgada_kw FROM bolt_energy_db.ralie_usina_csv_import;

SELECT * FROM bolt_energy_db.ralie_usina_empresa_potencia_gerada order by potencia desc;

SELECT sum(potencia) FROM bolt_energy_db.ralie_usina_empresa_potencia_gerada;


