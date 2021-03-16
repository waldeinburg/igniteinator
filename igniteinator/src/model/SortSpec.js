const ORDERS = {
    "ASC": 1,
    "DESC": 2
};

class SortSpec {
    /**
     * @param {String} field 
     * @param {Number} order 
     */
    constructor(field, order) {
        this.field = field
        this.order = order;
    }

    static get ASC() {
        return ORDERS.ASC;
    }

    static get DESC() {
        return ORDERS.DESC;
    }
}

export default SortSpec;
