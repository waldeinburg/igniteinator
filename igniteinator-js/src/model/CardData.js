/**
 * Class representing card data
 * 
 * This will probably only be used for type spec for code completion because the JSON data has the same fields anyway.
 */
class CardData {
    /**
     * @param {Number} id 
     * @param {String} name 
     * @param {String} cost 
     * @param {String} image 
     * @param {Number[]} combos 
     */
    constructor({id, name, cost, image, combos}) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.image = image;
        this.combos = combos;
    }
}

export default CardData;
