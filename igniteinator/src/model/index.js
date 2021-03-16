import { cards, keys } from "./generated/model";
import SortSpec from "./SortSpec";

const CARD_FIELDS = {
    ID: "id",
    NAME: "name",
    COST: "cost",
    IMAGE: "image",
    COMBOS: "combos"
};

const imageBaseUrl = "http://ec2-54-219-252-233.us-west-1.compute.amazonaws.com/uploads/";

/**
 * Desctructive sort array of cards
 * 
 * @param {CardData[]} cards 
 * @param {SortSpec[]} sortSpecs
 * @returns {Array}
 */
const sort = (cards, sortSpecs) => cards.sort((a, b) => {
    for (const spec of sortSpecs) {
        const f = spec.scoreFn;
        const aScore = f(a);
        const bScore = f(b);
        if (aScore !== bScore) {
            const o = spec.order === SortSpec.ASC ? 1 : -1;
            return aScore > bScore ? o : -o;
        }
    }
    return 0;
});

/**
 * Return array of cards in the order of the given ids
 * 
 * @param {String[]} ids 
 * @returns {CardData[]}
 */
const getCards = ids => ids.map(id => cards[id]);

/**
 * Return sorted array of cards
 * 
 * @param {String[]} ids 
 * @param {SortSpec[]} sortSpecs 
 * @returns {CardData[]}
 */
const getCardsSorted = (ids, sortSpecs) => sort(getCards(ids), sortSpecs);

const getFullImageUrl = path => imageBaseUrl + path;

export const testables = {
    sort: sort
};
export { getCardsSorted, getFullImageUrl, CARD_FIELDS, keys };
