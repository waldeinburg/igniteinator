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
const sortCards = (cards, sortSpecs) => cards.sort((a, b) => {
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
 * Filter array of cards
 * 
 * @param {CardData[]} cards 
 * @param {Function[]} filters 
 * @returns {CardData[]}
 */
const filterCards = (cards, filters) => {
    const fs = [...filters];
    const f = fs.shift();
    console.log(filters, fs, f);
    if (f === undefined)
        return cards;
    return filterCards(cards.filter(f), fs);
}

/**
 * Return all cards as an array
 * 
 * @returns {CardData[]}
 */
const getAllCards = () => getCards(Object.keys(cards))

/**
 * Return array of cards in the order of the given ids or all if undefined
 * 
 * @param {String[]} ids 
 * @returns {CardData[]}
 */
const getCards = ids => ids === undefined ?
    getAllCards() :
    ids.map(id => cards[id]);

/**
 * Return sorted array of cards
 * 
 * @param {String[]} ids 
 * @param {SortSpec[]} sortSpecs 
 * @returns {CardData[]}
 */
const getCardsFilteredAndSorted = (ids, filters, sortSpecs) =>
    sortCards(filterCards(getCards(ids), filters), sortSpecs);

const getFullImageUrl = path => imageBaseUrl + path;

export const testables = {
    sort: sortCards
};
export { getCardsFilteredAndSorted, getFullImageUrl, CARD_FIELDS, keys };
