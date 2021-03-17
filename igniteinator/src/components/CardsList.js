import Card from "./Card";

const CardsList = ({ cards }) => cards.map(card => (
    <Card card={card} />
));

export default CardsList;
