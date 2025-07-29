#Progetto mobile programming

##struttura logica
```mermaid
graph TD;
  login:nome-->controllo nome:se presente in DB;
  controllo nome:se presente in DB-->DB_nomi;
  controllo nome:se presente in DB--si-->main;
  controllo nome:se presente in DB--no-->Nuovo-utente;
  Nuovo-utente-->DB_nomi;
```
