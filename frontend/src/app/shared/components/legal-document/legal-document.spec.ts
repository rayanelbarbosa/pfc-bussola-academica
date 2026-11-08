import { parseInline, parseLegalDocument } from './legal-document';

describe('parseLegalDocument', () => {
  it('splits headings, paragraphs, lists and tables', () => {
    const blocks = parseLegalDocument(
      'Primeiro parágrafo\ncontinua aqui.\n\n## 1. Título\n- item um\n- item dois\n\n| A | B |\n| --- | --- |\n| 1 | 2 |'
    );

    expect(blocks.map((b) => b.kind)).toEqual(['paragraph', 'heading', 'list', 'table']);
    expect(blocks[0]).toEqual({ kind: 'paragraph', content: [{ kind: 'text', text: 'Primeiro parágrafo continua aqui.' }] });
    const list = blocks[2];
    expect(list.kind === 'list' && list.items.length).toBe(2);
    const table = blocks[3];
    expect(table.kind === 'table' && table.rows.length).toBe(1);
  });

  it('reads bold text and internal, external and e-mail links', () => {
    const segments = parseInline('Veja **isto** em [Meus dados](/meus-dados), [ANPD](https://www.gov.br/anpd) e [e-mail](mailto:a@b.com).');

    expect(segments.filter((s) => s.kind === 'bold').length).toBe(1);
    expect(segments).toContain({ kind: 'link', text: 'Meus dados', href: '/meus-dados', internal: true });
    expect(segments).toContain({ kind: 'link', text: 'ANPD', href: 'https://www.gov.br/anpd', internal: false });
    expect(segments).toContain({ kind: 'link', text: 'e-mail', href: 'mailto:a@b.com', internal: false });
  });

  it('never turns unsafe links or HTML into executable content', () => {
    const segments = parseInline('[clique](javascript:alert(1)) <script>alert(1)</script>');

    expect(segments.every((s) => s.kind === 'text')).toBeTrue();
  });
});
