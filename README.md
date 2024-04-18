# Treiber_Stack

### Характеристики вычислительной машины:
* Процессор: `11th Gen i7-11800H`
* Объем RAM: `16Gb`
* ОС: `6.1.80-1-MANJARO`

> **Пояснения**:
> * Время работы алгоритмы высчитывалось, как среднее от 10 запусков
> * Каждый поток выполнял 10^6 операций
> * Доступные операции: `pop`, `push`, `top`

### Сравнение времени работы TreiberStack и TreiberStack with Elimination

**Выбор операций производился рандомно:** 
<table>
    <tr>
        <td>Thread N</td>
        <td>TreiberStack</td>
        <td>EliminationTreiberStack</td>
    </tr>
    <tr>
        <td>1</td>
        <td>19</td>
        <td>16</td>
    </tr>
    <tr>
        <td>2</td>
        <td>40</td>
        <td>109</td>
    </tr>
    <tr>
        <td>4</td>
        <td>107</td>
        <td>336</td>
    </tr>
    <tr>
        <td>8</td>
        <td>327</td>
        <td>877</td>
    </tr>
    <tr>
        <td>16</td>
        <td>1166</td>
        <td>1791</td>
    </tr>
</table>

**Четные потоки делали `pop`, нечетные - `push`:**
<table>
    <tr>
        <td>Thread N</td>
        <td>TreiberStack</td>
        <td>EliminationTreiberStack</td>
    </tr>
    <tr>
        <td>1</td>
        <td>12</td>
        <td>10</td>
    </tr>
    <tr>
        <td>2</td>
        <td>28</td>
        <td>126</td>
    </tr>
    <tr>
        <td>4</td>
        <td>94</td>
        <td>469</td>
    </tr>
    <tr>
        <td>8</td>
        <td>321</td>
        <td>1212</td>
    </tr>
    <tr>
        <td>16</td>
        <td>1321</td>
        <td>2541</td>
    </tr>
</table>
