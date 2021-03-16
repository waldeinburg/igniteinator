const ORDERS = {
    "ASC": 1,
    "DESC": 2
};

class SortSpec {
    /**
     * @param {Function} scoreFn 
     * @param {Number} order 
     */
    constructor(scoreFn, order) {
        this._scoreFn = scoreFn
        this._order = order;
    }

    get scoreFn() {
        return this._scoreFn;
    }

    get order() {
        return this._order;
    }

    static get ASC() {
        return ORDERS.ASC;
    }

    static get DESC() {
        return ORDERS.DESC;
    }

    /**
     * @param {String} name 
     * @param {Number} order
     * @returns {SortSpec}
     */
    static fromField(name, order) {
        return new SortSpec(card => card[name], order);
    }
}

export default SortSpec;
