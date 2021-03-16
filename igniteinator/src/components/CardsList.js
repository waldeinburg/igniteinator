import Card from "./Card";

const CardsList = (props) => props.cards.map(card => (
    <Card card={card} />
));

export default CardsList;
